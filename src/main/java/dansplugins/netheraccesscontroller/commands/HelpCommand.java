package dansplugins.netheraccesscontroller.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class HelpCommand {

    public boolean execute(CommandSender sender) {
        sender.sendMessage(ChatColor.AQUA + "=== Nether Access Controller Commands ===");
        sender.sendMessage(ChatColor.AQUA + "/nac help");
        sender.sendMessage(ChatColor.AQUA + "/nac list");
        sender.sendMessage(ChatColor.AQUA + "/nac allow");
        sender.sendMessage(ChatColor.AQUA + "/nac deny");
        return true;
    }

}
