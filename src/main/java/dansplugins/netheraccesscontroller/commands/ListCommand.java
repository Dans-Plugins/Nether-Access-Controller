package dansplugins.netheraccesscontroller.commands;

import dansplugins.netheraccesscontroller.data.PersistentData;
import org.bukkit.command.CommandSender;

public class ListCommand {
    private final PersistentData persistentData;

    public ListCommand(PersistentData persistentData) {
        this.persistentData = persistentData;
    }

    public boolean execute(CommandSender sender) {
        persistentData.sendListToSender(sender);
        return false;
    }

}
