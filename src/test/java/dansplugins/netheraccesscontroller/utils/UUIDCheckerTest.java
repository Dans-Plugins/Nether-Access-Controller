package dansplugins.netheraccesscontroller.utils;

import dansplugins.netheraccesscontroller.commands.StubServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Covers how {@link UUIDChecker#findUUIDBasedOnPlayerName} turns the name typed after /nac allow
 * or /nac deny into a UUID: online players are searched before offline ones, names match without
 * regard to case, and an offline player with no recorded name is skipped rather than failing the
 * lookup.
 *
 * The online and offline players come from the shared {@link StubServer}, whose lists are emptied
 * before and after each test so that no other test class sees these players.
 */
class UUIDCheckerTest {

    private final UUIDChecker uuidChecker = new UUIDChecker();

    @BeforeEach
    void setUp() {
        StubServer.clearPlayerLists();
    }

    @AfterEach
    void tearDown() {
        StubServer.clearPlayerLists();
    }

    @Test
    void onlinePlayer_isFound() {
        UUID uuid = UUID.randomUUID();
        StubServer.addOnlinePlayer(uuid, "Steve");

        assertEquals(uuid, uuidChecker.findUUIDBasedOnPlayerName("Steve"));
    }

    @Test
    void offlinePlayer_isFoundWhenNoOnlinePlayerMatches() {
        UUID uuid = UUID.randomUUID();
        StubServer.addOnlinePlayer(UUID.randomUUID(), "Alex");
        StubServer.addOfflinePlayer(uuid, "Steve");

        assertEquals(uuid, uuidChecker.findUUIDBasedOnPlayerName("Steve"));
    }

    @Test
    void nameMatching_ignoresCase() {
        UUID onlineUUID = UUID.randomUUID();
        UUID offlineUUID = UUID.randomUUID();
        StubServer.addOnlinePlayer(onlineUUID, "Steve");
        StubServer.addOfflinePlayer(offlineUUID, "Alex");

        assertEquals(onlineUUID, uuidChecker.findUUIDBasedOnPlayerName("sTEVE"));
        assertEquals(offlineUUID, uuidChecker.findUUIDBasedOnPlayerName("ALEX"));
    }

    /**
     * Characterizes current behavior: when an online player and a different offline player share a
     * name, the online player's UUID is returned.
     */
    @Test
    void onlinePlayer_isPreferredOverOfflinePlayerWithTheSameName() {
        UUID onlineUUID = UUID.randomUUID();
        StubServer.addOfflinePlayer(UUID.randomUUID(), "Steve");
        StubServer.addOnlinePlayer(onlineUUID, "Steve");

        assertEquals(onlineUUID, uuidChecker.findUUIDBasedOnPlayerName("Steve"));
    }

    /**
     * Characterizes current behavior: when two offline players share a name, the first one the
     * server lists is returned.
     */
    @Test
    void firstMatchingOfflinePlayer_isReturned() {
        UUID firstUUID = UUID.randomUUID();
        StubServer.addOfflinePlayer(firstUUID, "Steve");
        StubServer.addOfflinePlayer(UUID.randomUUID(), "Steve");

        assertEquals(firstUUID, uuidChecker.findUUIDBasedOnPlayerName("Steve"));
    }

    @Test
    void unknownName_returnsNull() {
        StubServer.addOnlinePlayer(UUID.randomUUID(), "Steve");
        StubServer.addOfflinePlayer(UUID.randomUUID(), "Alex");

        assertNull(uuidChecker.findUUIDBasedOnPlayerName("Notch"));
    }

    @Test
    void noPlayersKnown_returnsNull() {
        assertNull(uuidChecker.findUUIDBasedOnPlayerName("Steve"));
    }

    /**
     * getName can return null for an offline player; the NullPointerException that follows is
     * caught and the search moves on to the next player.
     */
    @Test
    void offlinePlayerWithNoName_isSkipped() {
        UUID uuid = UUID.randomUUID();
        StubServer.addOfflinePlayer(UUID.randomUUID(), null);
        StubServer.addOfflinePlayer(uuid, "Steve");

        assertEquals(uuid, uuidChecker.findUUIDBasedOnPlayerName("Steve"));
    }

    @Test
    void offlinePlayerWithNoName_doesNotMatchAnyName() {
        StubServer.addOfflinePlayer(UUID.randomUUID(), null);

        assertNull(uuidChecker.findUUIDBasedOnPlayerName("Steve"));
    }
}
