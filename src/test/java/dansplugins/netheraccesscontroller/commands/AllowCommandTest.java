package dansplugins.netheraccesscontroller.commands;

import dansplugins.netheraccesscontroller.data.PersistentData;
import dansplugins.netheraccesscontroller.utils.UUIDChecker;
import org.bukkit.ChatColor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers how {@link AllowCommand#execute} adds a player to the whitelist: which inputs are refused
 * before the whitelist is touched, that a player already on it is not added twice, and which name
 * the confirmation message uses. DenyCommandTest covers the parallel removal path.
 *
 * Name lookup is replaced by a {@link UUIDChecker} subclass that answers from a map, and the
 * Bukkit server behind getOfflinePlayer is the shared {@link StubServer}.
 */
class AllowCommandTest {

    private final UUID playerUUID = UUID.randomUUID();
    private final StubUUIDChecker uuidChecker = new StubUUIDChecker();
    private final PersistentData persistentData = new PersistentData();
    private final StubServer.RecordingSender sender = new StubServer.RecordingSender();
    private final AllowCommand allowCommand = new AllowCommand(uuidChecker, persistentData);

    @BeforeEach
    void setUp() {
        StubServer.registerPlayer(playerUUID, "Steve");
        uuidChecker.uuids.put("steve", playerUUID);
    }

    @Test
    void noArguments_printsUsageWithoutAllowingAnyone() {
        boolean result = execute();

        assertFalse(result);
        assertEquals(1, sender.messages.size());
        assertEquals(ChatColor.RED + "Usage: /nac allow (playerName)", sender.messages.get(0));
        assertTrue(persistentData.getAllowedPlayers().isEmpty());
    }

    @Test
    void unknownPlayer_isRefusedWithoutAllowingAnyone() {
        boolean result = execute("Alex");

        assertFalse(result);
        assertEquals(1, sender.messages.size());
        assertEquals(ChatColor.RED + "That player wasn't found.", sender.messages.get(0));
        assertTrue(persistentData.getAllowedPlayers().isEmpty());
    }

    @Test
    void knownPlayer_isAddedToTheWhitelist() {
        boolean result = execute("Steve");

        assertTrue(result);
        assertTrue(persistentData.isPlayerAllowed(playerUUID));
        assertEquals(1, sender.messages.size());
        assertEquals(ChatColor.GREEN + "Steve is now allowed to use nether portals.", sender.messages.get(0));
    }

    /**
     * The confirmation names the player as the server knows them, not as the command was typed.
     */
    @Test
    void confirmation_usesTheServersNameForThePlayer() {
        execute("sTeVe");

        assertEquals(ChatColor.GREEN + "Steve is now allowed to use nether portals.", sender.messages.get(0));
    }

    /**
     * PersistentData has no duplicate guard of its own, so this check is what keeps a second
     * /nac allow from adding the same UUID twice; see PersistentDataTest.
     */
    @Test
    void alreadyAllowedPlayer_isRefusedAndNotAddedTwice() {
        persistentData.setPlayerAllowed(playerUUID, true);

        boolean result = execute("Steve");

        assertFalse(result);
        assertEquals(1, persistentData.getAllowedPlayers().size());
        assertEquals(1, sender.messages.size());
        assertEquals(ChatColor.RED + "Steve is already allowed to use nether portals.", sender.messages.get(0));
    }

    @Test
    void allowingOnePlayer_leavesOtherPlayersUnchanged() {
        UUID otherPlayerUUID = UUID.randomUUID();
        StubServer.registerPlayer(otherPlayerUUID, "Alex");
        uuidChecker.uuids.put("alex", otherPlayerUUID);
        persistentData.setPlayerAllowed(otherPlayerUUID, true);

        execute("Steve");

        assertTrue(persistentData.isPlayerAllowed(playerUUID));
        assertTrue(persistentData.isPlayerAllowed(otherPlayerUUID));
        assertEquals(2, persistentData.getAllowedPlayers().size());
    }

    /**
     * Characterizes current behavior: only the first argument is read as the player's name.
     */
    @Test
    void extraArguments_areIgnored() {
        boolean result = execute("Steve", "Alex");

        assertTrue(result);
        assertTrue(persistentData.isPlayerAllowed(playerUUID));
        assertEquals(1, persistentData.getAllowedPlayers().size());
    }

    private boolean execute(String... args) {
        return allowCommand.execute(StubServer.sender(sender), args);
    }

    /**
     * Answers name lookups from a map keyed by lower-case name, matching the case-insensitive
     * lookup in {@link UUIDChecker}, instead of searching the server's players.
     */
    static class StubUUIDChecker extends UUIDChecker {
        final Map<String, UUID> uuids = new HashMap<>();

        @Override
        public UUID findUUIDBasedOnPlayerName(String playerName) {
            return uuids.get(playerName.toLowerCase());
        }
    }
}
