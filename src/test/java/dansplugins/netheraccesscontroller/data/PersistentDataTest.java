package dansplugins.netheraccesscontroller.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the UUID-based whitelist logic in {@link PersistentData}, which is the
 * data structure backing every allow/deny access decision made by the plugin.
 * Methods that require a live Bukkit server (e.g. sendListToSender) are out of
 * scope here and are covered by manual validation instead.
 */
class PersistentDataTest {

    private PersistentData persistentData;
    private UUID playerUUID;

    @BeforeEach
    void setUp() {
        persistentData = new PersistentData();
        playerUUID = UUID.randomUUID();
    }

    @Test
    void isPlayerAllowed_returnsFalse_forPlayerNeverAdded() {
        assertFalse(persistentData.isPlayerAllowed(playerUUID));
    }

    @Test
    void setPlayerAllowed_true_addsPlayerToWhitelist() {
        persistentData.setPlayerAllowed(playerUUID, true);

        assertTrue(persistentData.isPlayerAllowed(playerUUID));
        assertTrue(persistentData.getAllowedPlayers().contains(playerUUID));
    }

    @Test
    void setPlayerAllowed_false_removesPlayerFromWhitelist() {
        persistentData.setPlayerAllowed(playerUUID, true);

        persistentData.setPlayerAllowed(playerUUID, false);

        assertFalse(persistentData.isPlayerAllowed(playerUUID));
        assertFalse(persistentData.getAllowedPlayers().contains(playerUUID));
    }

    @Test
    void setPlayerAllowed_false_onPlayerNotOnWhitelist_isNoOp() {
        persistentData.setPlayerAllowed(playerUUID, false);

        assertFalse(persistentData.isPlayerAllowed(playerUUID));
    }

    @Test
    void setPlayerAllowed_doesNotAffectOtherPlayers() {
        UUID otherPlayerUUID = UUID.randomUUID();
        persistentData.setPlayerAllowed(playerUUID, true);

        persistentData.setPlayerAllowed(otherPlayerUUID, false);

        assertTrue(persistentData.isPlayerAllowed(playerUUID));
        assertFalse(persistentData.isPlayerAllowed(otherPlayerUUID));
    }

    @Test
    void setPlayerAllowed_true_calledTwiceForSamePlayer_addsDuplicateEntry() {
        // Characterizes current behavior: PersistentData itself has no
        // dedup guard, so callers (e.g. AllowCommand) are responsible for
        // checking isPlayerAllowed before adding. This is not asserting
        // desired behavior, only the behavior as it exists today.
        persistentData.setPlayerAllowed(playerUUID, true);
        persistentData.setPlayerAllowed(playerUUID, true);

        long occurrences = persistentData.getAllowedPlayers().stream()
                .filter(uuid -> uuid.equals(playerUUID))
                .count();
        assertEquals(2, occurrences);
    }

    @Test
    void saveThenLoad_roundTripsAllowedPlayers() {
        UUID otherPlayerUUID = UUID.randomUUID();
        persistentData.setPlayerAllowed(playerUUID, true);
        persistentData.setPlayerAllowed(otherPlayerUUID, true);

        Map<String, String> saved = persistentData.save();

        PersistentData reloaded = new PersistentData();
        reloaded.load(saved);

        assertTrue(reloaded.isPlayerAllowed(playerUUID));
        assertTrue(reloaded.isPlayerAllowed(otherPlayerUUID));
        assertEquals(2, reloaded.getAllowedPlayers().size());
    }

    @Test
    void load_withNoPreviouslySavedPlayers_resultsInEmptyWhitelist() {
        PersistentData source = new PersistentData();
        Map<String, String> saved = source.save();

        persistentData.load(saved);

        assertTrue(persistentData.getAllowedPlayers().isEmpty());
    }
}
