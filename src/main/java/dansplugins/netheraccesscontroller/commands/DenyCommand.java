package dansplugins.netheraccesscontroller.commands;

import dansplugins.netheraccesscontroller.PersistentData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DenyCommand {

    public boolean execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            return false;
        }
        String playerName = args[0];
        Player player = Bukkit.getPlayer(playerName);

        if (player == null) {
            sender.sendMessage(ChatColor.RED + "That player wasn't found.");
            return false;
        }

        if (!PersistentData.getInstance().isPlayerAllowed(player)) {
            sender.sendMessage(ChatColor.RED + player.getName() + " is already not allowed to use nether portals.");
            return false;
        }

        PersistentData.getInstance().setPlayerAllowed(player, false);
        return true;
    }

}
