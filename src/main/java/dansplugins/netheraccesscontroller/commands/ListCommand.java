package dansplugins.netheraccesscontroller.commands;

import dansplugins.netheraccesscontroller.data.PersistentData;
import org.bukkit.command.CommandSender;

public class ListCommand {

    public boolean execute(CommandSender sender) {
        PersistentData.getInstance().sendListToSender(sender);
        return false;
    }

}
