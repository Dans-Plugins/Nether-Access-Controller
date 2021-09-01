package dansplugins.netheraccesscontroller.commands;

import dansplugins.netheraccesscontroller.PersistentData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AllowCommand {

    public boolean execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Not enough arguments.");
            return false;
        }
        String playerName = args[0];
        Player player = Bukkit.getPlayer(playerName);
        PersistentData.getInstance().setPlayerAllowed(player, true);
        sender.sendMessage(ChatColor.GREEN + player.getName() + " is now allowed to use nether portals.");
        return true;
    }

}
