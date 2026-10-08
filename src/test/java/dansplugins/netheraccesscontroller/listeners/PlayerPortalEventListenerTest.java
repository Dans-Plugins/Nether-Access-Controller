package dansplugins.netheraccesscontroller.listeners;

import dansplugins.netheraccesscontroller.NetherAccessController;
import dansplugins.netheraccesscontroller.data.PersistentData;
import dansplugins.netheraccesscontroller.services.ConfigService;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers {@link PlayerPortalEventListener#handle}, which decides whether a player may travel
 * through a nether portal: that a player not on the whitelist is stopped and told why only while
 * preventPortalUsage is on, that a whitelisted player is let through, and that portals other than
 * nether portals are left alone. They pin current behaviour; see ListenerFixtures for how the
 * plugin is stood up without a server.
 */
class PlayerPortalEventListenerTest {

    private final FileConfiguration config = ListenerFixtures.defaultConfig();
    private final PersistentData persistentData = new PersistentData();
    private final ListenerFixtures.RecordingPlayer player = new ListenerFixtures.RecordingPlayer();
    private final NetherAccessController plugin = ListenerFixtures.pluginWith(config);
    private final PlayerPortalEventListener listener = new PlayerPortalEventListener(plugin, new ConfigService(plugin), persistentData);

    /**
     * The shipped default: only portal creation is controlled, so an existing portal admits anyone.
     */
    @Test
    void usagePreventionOff_letsAPlayerNotOnTheWhitelistThrough() {
        PlayerPortalEvent event = portalEvent(TeleportCause.NETHER_PORTAL);

        listener.handle(event);

        assertFalse(event.isCancelled());
        assertTrue(player.messages.isEmpty());
    }

    @Test
    void usagePreventionOn_stopsAPlayerNotOnTheWhitelist() {
        config.set("preventPortalUsage", true);
        PlayerPortalEvent event = portalEvent(TeleportCause.NETHER_PORTAL);

        listener.handle(event);

        assertTrue(event.isCancelled());
        assertEquals(1, player.messages.size());
        assertEquals(ChatColor.RED + "You're unable to use nether portals.", player.messages.get(0));
    }

    @Test
    void usagePreventionOn_letsAWhitelistedPlayerThrough() {
        config.set("preventPortalUsage", true);
        persistentData.setPlayerAllowed(player.uuid, true);
        PlayerPortalEvent event = portalEvent(TeleportCause.NETHER_PORTAL);

        listener.handle(event);

        assertFalse(event.isCancelled());
        assertEquals(1, player.messages.size());
        assertEquals(ChatColor.GREEN + "You step through the portal.", player.messages.get(0));
    }

    @Test
    void denial_usesTheConfiguredDenyUsageMessage() {
        config.set("preventPortalUsage", true);
        config.set("denyUsageMessage", "The nether is closed.");
        PlayerPortalEvent event = portalEvent(TeleportCause.NETHER_PORTAL);

        listener.handle(event);

        assertEquals(ChatColor.RED + "The nether is closed.", player.messages.get(0));
    }

    /**
     * Only nether portals are controlled: an end portal admits a player not on the whitelist even
     * while usage prevention is on.
     */
    @Test
    void usagePreventionOn_leavesEndPortalsAlone() {
        config.set("preventPortalUsage", true);
        PlayerPortalEvent event = portalEvent(TeleportCause.END_PORTAL);

        listener.handle(event);

        assertFalse(event.isCancelled());
        assertTrue(player.messages.isEmpty());
    }

    /**
     * A player removed from the whitelist is stopped at the next portal, not only players who were
     * never on it.
     */
    @Test
    void usagePreventionOn_stopsAPlayerWhoseAccessWasRevoked() {
        config.set("preventPortalUsage", true);
        persistentData.setPlayerAllowed(player.uuid, true);
        persistentData.setPlayerAllowed(player.uuid, false);
        PlayerPortalEvent event = portalEvent(TeleportCause.NETHER_PORTAL);

        listener.handle(event);

        assertTrue(event.isCancelled());
    }

    private PlayerPortalEvent portalEvent(TeleportCause cause) {
        return new PlayerPortalEvent(player.player(), new Location(null, 0, 64, 0), new Location(null, 0, 64, 0), cause);
    }
}
