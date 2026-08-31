package dansplugins.netheraccesscontroller.commands;

import dansplugins.netheraccesscontroller.data.PersistentData;
import org.bukkit.command.CommandSender;

/**
 * @author Daniel McCoy Stephenson
 */
public class ListCommand {
    private final PersistentData persistentData;

    public ListCommand(PersistentData persistentData) {
        this.persistentData = persistentData;
    }

    /**
     * Returns true because the whitelist was printed. The value reaches Bukkit as the result of
     * onCommand, where false means the command was used incorrectly and asks the server to print
     * the command's usage string after the output above.
     */
    public boolean execute(CommandSender sender) {
        persistentData.sendListToSender(sender);
        return true;
    }

}
