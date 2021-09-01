package dansplugins.netheraccesscontroller.commands;

import dansplugins.netheraccesscontroller.PersistentData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AllowCommand {

    public boolean execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /nac allow (playerName)");
            return false;
        }
        String playerName = args[0];
        Player player = Bukkit.getPlayer(playerName);

        if (player == null) {
            sender.sendMessage(ChatColor.RED + "That player wasn't found.");
            return false;
        }

        if (PersistentData.getInstance().isPlayerAllowed(player)) {
            sender.sendMessage(ChatColor.RED + player.getName() + " is already allowed to use nether portals.");
            return false;
        }

        PersistentData.getInstance().setPlayerAllowed(player, true);
        sender.sendMessage(ChatColor.GREEN + player.getName() + " is now allowed to use nether portals.");
        return true;
    }

}
