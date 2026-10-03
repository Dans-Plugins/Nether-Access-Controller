package dansplugins.netheraccesscontroller.commands;

import dansplugins.netheraccesscontroller.data.PersistentData;
import org.bukkit.ChatColor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers how {@link DenyCommand#execute} removes a player from the whitelist: which inputs are
 * refused before the whitelist is touched, that a player not on it is reported rather than
 * removed, and which name the confirmation message uses. AllowCommandTest covers the parallel
 * addition path and supplies the stubbed name lookup used here.
 */
class DenyCommandTest {

    private final UUID playerUUID = UUID.randomUUID();
    private final AllowCommandTest.StubUUIDChecker uuidChecker = new AllowCommandTest.StubUUIDChecker();
    private final PersistentData persistentData = new PersistentData();
    private final StubServer.RecordingSender sender = new StubServer.RecordingSender();
    private final DenyCommand denyCommand = new DenyCommand(uuidChecker, persistentData);

    @BeforeEach
    void setUp() {
        StubServer.registerPlayer(playerUUID, "Steve");
        uuidChecker.uuids.put("steve", playerUUID);
    }

    @Test
    void noArguments_printsUsageWithoutDenyingAnyone() {
        persistentData.setPlayerAllowed(playerUUID, true);

        boolean result = execute();

        assertFalse(result);
        assertEquals(1, sender.messages.size());
        assertEquals(ChatColor.RED + "Usage: /nac deny (playerName)", sender.messages.get(0));
        assertTrue(persistentData.isPlayerAllowed(playerUUID));
    }

    @Test
    void unknownPlayer_isRefusedWithoutDenyingAnyone() {
        persistentData.setPlayerAllowed(playerUUID, true);

        boolean result = execute("Alex");

        assertFalse(result);
        assertEquals(1, sender.messages.size());
        assertEquals(ChatColor.RED + "That player wasn't found.", sender.messages.get(0));
        assertTrue(persistentData.isPlayerAllowed(playerUUID));
    }

    @Test
    void allowedPlayer_isRemovedFromTheWhitelist() {
        persistentData.setPlayerAllowed(playerUUID, true);

        boolean result = execute("Steve");

        assertTrue(result);
        assertFalse(persistentData.isPlayerAllowed(playerUUID));
        assertTrue(persistentData.getAllowedPlayers().isEmpty());
        assertEquals(1, sender.messages.size());
        assertEquals(ChatColor.GREEN + "Steve is no longer allowed to use nether portals.", sender.messages.get(0));
    }

    /**
     * The confirmation names the player as the server knows them, not as the command was typed.
     */
    @Test
    void confirmation_usesTheServersNameForThePlayer() {
        persistentData.setPlayerAllowed(playerUUID, true);

        execute("sTeVe");

        assertEquals(ChatColor.GREEN + "Steve is no longer allowed to use nether portals.", sender.messages.get(0));
    }

    @Test
    void playerNotOnTheWhitelist_isRefused() {
        boolean result = execute("Steve");

        assertFalse(result);
        assertFalse(persistentData.isPlayerAllowed(playerUUID));
        assertEquals(1, sender.messages.size());
        assertEquals(ChatColor.RED + "Steve is already not allowed to use nether portals.", sender.messages.get(0));
    }

    @Test
    void denyingOnePlayer_leavesOtherPlayersAllowed() {
        UUID otherPlayerUUID = UUID.randomUUID();
        StubServer.registerPlayer(otherPlayerUUID, "Alex");
        persistentData.setPlayerAllowed(playerUUID, true);
        persistentData.setPlayerAllowed(otherPlayerUUID, true);

        execute("Steve");

        assertFalse(persistentData.isPlayerAllowed(playerUUID));
        assertTrue(persistentData.isPlayerAllowed(otherPlayerUUID));
        assertEquals(1, persistentData.getAllowedPlayers().size());
    }

    /**
     * Characterizes current behavior: only the first argument is read as the player's name.
     */
    @Test
    void extraArguments_areIgnored() {
        persistentData.setPlayerAllowed(playerUUID, true);

        boolean result = execute("Steve", "Alex");

        assertTrue(result);
        assertFalse(persistentData.isPlayerAllowed(playerUUID));
    }

    private boolean execute(String... args) {
        return denyCommand.execute(StubServer.sender(sender), args);
    }
}
