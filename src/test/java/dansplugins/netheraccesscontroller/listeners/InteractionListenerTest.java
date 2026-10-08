package dansplugins.netheraccesscontroller.listeners;

import dansplugins.netheraccesscontroller.NetherAccessController;
import dansplugins.netheraccesscontroller.data.PersistentData;
import dansplugins.netheraccesscontroller.services.ConfigService;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers {@link InteractionListener#handle}, which decides whether a player may light a nether
 * portal: that a player not on the whitelist who uses flint and steel on obsidian is stopped and
 * told why while preventPortalCreation is on, that a whitelisted player is not, and which
 * interactions are left alone. They pin current behaviour; see ListenerFixtures for how the plugin
 * is stood up without a server.
 */
class InteractionListenerTest {

    private final FileConfiguration config = ListenerFixtures.defaultConfig();
    private final PersistentData persistentData = new PersistentData();
    private final ListenerFixtures.RecordingPlayer player = new ListenerFixtures.RecordingPlayer();
    private final NetherAccessController plugin = ListenerFixtures.pluginWith(config);
    private final InteractionListener listener = new InteractionListener(plugin, new ConfigService(plugin), persistentData);

    /**
     * The shipped default: creation prevention is on, so lighting obsidian is refused.
     */
    @Test
    void flintAndSteelOnObsidian_isRefusedForAPlayerNotOnTheWhitelist() {
        player.mainHand = new ItemStack(Material.FLINT_AND_STEEL);
        PlayerInteractEvent event = interact(Action.RIGHT_CLICK_BLOCK, ListenerFixtures.block(Material.OBSIDIAN));

        listener.handle(event);

        assertTrue(event.isCancelled());
        assertEquals(1, player.messages.size());
        assertEquals(ChatColor.RED + "You're unable to create nether portals.", player.messages.get(0));
    }

    @Test
    void flintAndSteelInTheOffHand_isRefusedForAPlayerNotOnTheWhitelist() {
        player.offHand = new ItemStack(Material.FLINT_AND_STEEL);
        PlayerInteractEvent event = interact(Action.RIGHT_CLICK_BLOCK, ListenerFixtures.block(Material.OBSIDIAN));

        listener.handle(event);

        assertTrue(event.isCancelled());
    }

    @Test
    void flintAndSteelOnObsidian_isAllowedForAWhitelistedPlayer() {
        persistentData.setPlayerAllowed(player.uuid, true);
        player.mainHand = new ItemStack(Material.FLINT_AND_STEEL);
        PlayerInteractEvent event = interact(Action.RIGHT_CLICK_BLOCK, ListenerFixtures.block(Material.OBSIDIAN));

        listener.handle(event);

        assertFalse(event.isCancelled());
        assertEquals(1, player.messages.size());
        assertEquals(ChatColor.GREEN + "You light the portal.", player.messages.get(0));
    }

    @Test
    void denial_usesTheConfiguredDenyCreationMessage() {
        config.set("denyCreationMessage", "No portals for you.");
        player.mainHand = new ItemStack(Material.FLINT_AND_STEEL);
        PlayerInteractEvent event = interact(Action.RIGHT_CLICK_BLOCK, ListenerFixtures.block(Material.OBSIDIAN));

        listener.handle(event);

        assertEquals(ChatColor.RED + "No portals for you.", player.messages.get(0));
    }

    @Test
    void creationPreventionOff_letsAPlayerNotOnTheWhitelistLightObsidian() {
        config.set("preventPortalCreation", false);
        player.mainHand = new ItemStack(Material.FLINT_AND_STEEL);
        PlayerInteractEvent event = interact(Action.RIGHT_CLICK_BLOCK, ListenerFixtures.block(Material.OBSIDIAN));

        listener.handle(event);

        assertFalse(event.isCancelled());
        assertTrue(player.messages.isEmpty());
    }

    @Test
    void flintAndSteelOnAnotherBlock_isLeftAlone() {
        player.mainHand = new ItemStack(Material.FLINT_AND_STEEL);
        PlayerInteractEvent event = interact(Action.RIGHT_CLICK_BLOCK, ListenerFixtures.block(Material.NETHERRACK));

        listener.handle(event);

        assertFalse(event.isCancelled());
        assertTrue(player.messages.isEmpty());
    }

    @Test
    void obsidianClickedWithoutFlintAndSteel_isLeftAlone() {
        player.mainHand = new ItemStack(Material.DIAMOND_PICKAXE);
        PlayerInteractEvent event = interact(Action.RIGHT_CLICK_BLOCK, ListenerFixtures.block(Material.OBSIDIAN));

        listener.handle(event);

        assertFalse(event.isCancelled());
        assertTrue(player.messages.isEmpty());
    }

    @Test
    void interactionWithNoBlock_isLeftAlone() {
        player.mainHand = new ItemStack(Material.FLINT_AND_STEEL);
        PlayerInteractEvent event = interact(Action.RIGHT_CLICK_AIR, null);

        listener.handle(event);

        assertTrue(player.messages.isEmpty());
    }

    /**
     * Characterizes current behaviour: only flint and steel is recognised as a way of lighting a
     * portal, so a fire charge used on obsidian is not refused.
     */
    @Test
    void fireChargeOnObsidian_isNotRefused() {
        player.mainHand = new ItemStack(Material.FIRE_CHARGE);
        PlayerInteractEvent event = interact(Action.RIGHT_CLICK_BLOCK, ListenerFixtures.block(Material.OBSIDIAN));

        listener.handle(event);

        assertFalse(event.isCancelled());
        assertTrue(player.messages.isEmpty());
    }

    /**
     * Characterizes current behaviour: the kind of click is not checked, so left-clicking obsidian
     * while holding flint and steel is refused in the same way as lighting it.
     */
    @Test
    void leftClickOnObsidianWithFlintAndSteel_isAlsoRefused() {
        player.mainHand = new ItemStack(Material.FLINT_AND_STEEL);
        PlayerInteractEvent event = interact(Action.LEFT_CLICK_BLOCK, ListenerFixtures.block(Material.OBSIDIAN));

        listener.handle(event);

        assertTrue(event.isCancelled());
        assertEquals(ChatColor.RED + "You're unable to create nether portals.", player.messages.get(0));
    }

    private PlayerInteractEvent interact(Action action, Block clickedBlock) {
        return new PlayerInteractEvent(player.player(), action, player.mainHand, clickedBlock, BlockFace.UP);
    }
}
