package dansplugins.netheraccesscontroller.data;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the UUID-based whitelist logic in {@link PersistentData}, which is the
 * data structure backing every allow/deny access decision made by the plugin.
 * sendListToSender looks players up through the Bukkit server, so it is covered
 * through /nac list in ListCommandTest instead.
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

    /**
     * The only way into the list twice in play is a hand edit of allowedPlayers.json, because
     * AllowCommand checks first. Removing one copy would leave the player allowed while
     * /nac deny reported that they no longer are.
     */
    @Test
    void setPlayerAllowed_false_removesEveryCopyOfADuplicatedEntry() {
        UUID otherPlayerUUID = UUID.randomUUID();
        Map<String, String> saved = new HashMap<String, String>();
        saved.put("allowedPlayers",
                "[\"" + playerUUID + "\",\"" + otherPlayerUUID + "\",\"" + playerUUID + "\"]");
        assertTrue(persistentData.load(saved));

        persistentData.setPlayerAllowed(playerUUID, false);

        assertFalse(persistentData.isPlayerAllowed(playerUUID));
        assertTrue(persistentData.isPlayerAllowed(otherPlayerUUID));
    }

    @Test
    void setPlayerAllowed_false_forAPlayer_removesEveryCopyOfADuplicatedEntry() {
        Player player = (Player) Proxy.newProxyInstance(
                Player.class.getClassLoader(), new Class<?>[]{Player.class},
                (proxy, method, args) -> method.getName().equals("getUniqueId") ? playerUUID : null);
        persistentData.setPlayerAllowed(playerUUID, true);
        persistentData.setPlayerAllowed(playerUUID, true);

        persistentData.setPlayerAllowed(player, false);

        assertFalse(persistentData.isPlayerAllowed(playerUUID));
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

    /**
     * Save data that carries no usable whitelist must leave an empty whitelist behind rather
     * than a null one. A null whitelist makes isPlayerAllowed throw out of the listener that
     * called it, which leaves the portal event uncancelled and lets a player who was never
     * allowed through — the opposite of what this plugin exists to do. Each case below asserts
     * the deny-everyone fallback by calling isPlayerAllowed, which is the method the listeners
     * themselves call.
     */
    @Test
    void load_withSaveDataMissingTheWhitelistKey_deniesEveryone() {
        persistentData.load(new HashMap<String, String>());

        assertTrue(persistentData.getAllowedPlayers().isEmpty());
        assertFalse(persistentData.isPlayerAllowed(playerUUID));
    }

    @Test
    void load_withNullWhitelistValue_deniesEveryone() {
        Map<String, String> saved = new HashMap<String, String>();
        saved.put("allowedPlayers", null);

        persistentData.load(saved);

        assertTrue(persistentData.getAllowedPlayers().isEmpty());
        assertFalse(persistentData.isPlayerAllowed(playerUUID));
    }

    @Test
    void load_withUnparseableWhitelistValue_deniesEveryone() {
        Map<String, String> saved = new HashMap<String, String>();
        saved.put("allowedPlayers", "[\"" + playerUUID + "\"");

        persistentData.load(saved);

        assertTrue(persistentData.getAllowedPlayers().isEmpty());
        assertFalse(persistentData.isPlayerAllowed(playerUUID));
    }

    @Test
    void load_withNullSaveData_deniesEveryone() {
        persistentData.load(null);

        assertTrue(persistentData.getAllowedPlayers().isEmpty());
        assertFalse(persistentData.isPlayerAllowed(playerUUID));
    }

    @Test
    void load_withUnusableSaveData_discardsAnyPreviouslyLoadedWhitelist() {
        persistentData.setPlayerAllowed(playerUUID, true);

        persistentData.load(new HashMap<String, String>());

        assertFalse(persistentData.isPlayerAllowed(playerUUID));
    }

    /**
     * Gson reads the save data leniently, so a comma left behind by a hand edit of
     * allowedPlayers.json loads as a null entry. A null entry that stayed in the list would make
     * isPlayerAllowed throw out of the listener, with the same result as a null whitelist.
     */
    @Test
    void load_withATrailingComma_keepsTheEntriesAndStillDeniesOthers() {
        Map<String, String> saved = new HashMap<String, String>();
        saved.put("allowedPlayers", "[\"" + playerUUID + "\",]");

        assertTrue(persistentData.load(saved));

        assertEquals(1, persistentData.getAllowedPlayers().size());
        assertTrue(persistentData.isPlayerAllowed(playerUUID));
        assertFalse(persistentData.isPlayerAllowed(UUID.randomUUID()));
    }

    @Test
    void load_withADoubledComma_keepsTheEntriesOnBothSides() {
        UUID otherPlayerUUID = UUID.randomUUID();
        Map<String, String> saved = new HashMap<String, String>();
        saved.put("allowedPlayers", "[\"" + playerUUID + "\",,\"" + otherPlayerUUID + "\"]");

        assertTrue(persistentData.load(saved));

        assertEquals(2, persistentData.getAllowedPlayers().size());
        assertTrue(persistentData.isPlayerAllowed(otherPlayerUUID));
        assertFalse(persistentData.isPlayerAllowed(UUID.randomUUID()));
    }

    /**
     * Which exception an invalid UUID raises depends on the Gson version the server provides, and
     * one that escapes here stops the plugin from enabling.
     */
    @Test
    void load_withAnInvalidUuid_deniesEveryoneAndReportsThatNothingWasRecovered() {
        Map<String, String> saved = new HashMap<String, String>();
        saved.put("allowedPlayers", "[\"" + playerUUID + "\",\"c0ffee\"]");

        assertFalse(persistentData.load(saved));

        assertTrue(persistentData.getAllowedPlayers().isEmpty());
        assertFalse(persistentData.isPlayerAllowed(playerUUID));
    }

    @Test
    void load_reportsWhetherAWhitelistWasRecovered() {
        assertTrue(persistentData.load(new PersistentData().save()));
        assertFalse(persistentData.load(new HashMap<String, String>()));
        assertFalse(persistentData.load(null));
    }

    @Test
    void listHeader_hasAsManyEqualsSignsOnEachSide() {
        String header = PersistentData.LIST_HEADER;
        String title = header.replaceAll("^=+ ", "").replaceAll(" =+$", "");
        int leading = header.indexOf(title) - 1;
        int trailing = header.length() - header.indexOf(title) - title.length() - 1;
        assertEquals("Players With Access To The Nether", title);
        assertEquals(leading, trailing);
    }
}
