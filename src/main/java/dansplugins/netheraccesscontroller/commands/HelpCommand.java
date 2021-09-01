package dansplugins.netheraccesscontroller.commands;

import org.bukkit.command.CommandSender;

public class HelpCommand {

    public boolean execute(CommandSender sender) {
        sender.sendMessage("/nac help");
        sender.sendMessage(("/nac list"));
        sender.sendMessage("/nac allow");
        sender.sendMessage("/nac deny");
        return true;
    }

}
