package dansplugins.netheraccesscontroller.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 * @author Daniel McCoy Stephenson
 */
public class HelpCommand {

    public boolean execute(CommandSender sender) {
        sender.sendMessage(ChatColor.AQUA + "=== Nether Access Controller Commands ===");
        sender.sendMessage(ChatColor.AQUA + "/nac help - View a list of helpful commands.");
        sender.sendMessage(ChatColor.AQUA + "/nac list - List players who are allowed to access the nether.");
        sender.sendMessage(ChatColor.AQUA + "/nac allow - Allow a player to access the nether.");
        sender.sendMessage(ChatColor.AQUA + "/nac deny - Disallow a player from accessing the nether.");
        sender.sendMessage(ChatColor.AQUA + "/nac config - View or set config options.");
        return true;
    }

}
