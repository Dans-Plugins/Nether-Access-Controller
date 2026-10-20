package dansplugins.netheraccesscontroller.commands;

import org.bukkit.ChatColor;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the command list {@link HelpCommand#execute} prints. Each line's syntax is the one
 * COMMANDS.md documents, so a change here is a change to the documented commands as well.
 */
class HelpCommandTest {

    @Test
    void printsEverySubCommandWithItsSyntax() {
        StubServer.RecordingSender sender = new StubServer.RecordingSender();

        boolean result = new HelpCommand().execute(StubServer.sender(sender));

        assertTrue(result);
        assertEquals(Arrays.asList(
                ChatColor.AQUA + "=== Nether Access Controller Commands ===",
                ChatColor.AQUA + "/nac help - View a list of helpful commands.",
                ChatColor.AQUA + "/nac list - List players who are allowed to access the nether.",
                ChatColor.AQUA + "/nac allow <ign> - Allow a player to access the nether.",
                ChatColor.AQUA + "/nac deny <ign> - Disallow a player from accessing the nether.",
                ChatColor.AQUA + "/nac config <show | set> - View or set config options."), sender.messages);
    }
}
