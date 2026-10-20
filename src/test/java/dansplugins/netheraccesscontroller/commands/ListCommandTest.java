package dansplugins.netheraccesscontroller.commands;

import dansplugins.netheraccesscontroller.data.PersistentData;
import org.bukkit.ChatColor;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers what {@link ListCommand#execute} prints, which is the whitelist as
 * {@link PersistentData#sendListToSender} formats it: a header followed by one line per allowed
 * player, named as the server knows them, or a single line when no one is allowed.
 *
 * The Bukkit server behind getOfflinePlayer is the shared {@link StubServer}.
 */
class ListCommandTest {

    private final PersistentData persistentData = new PersistentData();
    private final StubServer.RecordingSender sender = new StubServer.RecordingSender();
    private final ListCommand listCommand = new ListCommand(persistentData);

    @Test
    void emptyWhitelist_printsThatNoOneIsAllowed() {
        boolean result = execute();

        assertTrue(result);
        assertEquals(Arrays.asList(ChatColor.AQUA + "No one is allowed to access the nether."), sender.messages);
    }

    @Test
    void allowedPlayers_arePrintedUnderTheHeaderInTheOrderTheyWereAllowed() {
        UUID steveUUID = UUID.randomUUID();
        UUID alexUUID = UUID.randomUUID();
        StubServer.registerPlayer(steveUUID, "Steve");
        StubServer.registerPlayer(alexUUID, "Alex");
        persistentData.setPlayerAllowed(steveUUID, true);
        persistentData.setPlayerAllowed(alexUUID, true);

        boolean result = execute();

        assertTrue(result);
        assertEquals(Arrays.asList(
                ChatColor.AQUA + "=== Players With Access To The Nether ===",
                ChatColor.AQUA + "Steve",
                ChatColor.AQUA + "Alex"), sender.messages);
    }

    /**
     * Characterizes current behavior: the server answers getOfflinePlayer for any UUID, with a
     * player whose name is null when it has no name recorded for that UUID, and the list prints
     * that null as the text "null" rather than skipping the entry or showing the UUID.
     */
    @Test
    void allowedPlayerWithNoRecordedName_isPrintedAsNull() {
        persistentData.setPlayerAllowed(UUID.randomUUID(), true);

        execute();

        assertEquals(Arrays.asList(
                ChatColor.AQUA + "=== Players With Access To The Nether ===",
                ChatColor.AQUA + "null"), sender.messages);
    }

    private boolean execute() {
        return listCommand.execute(StubServer.sender(sender));
    }
}
