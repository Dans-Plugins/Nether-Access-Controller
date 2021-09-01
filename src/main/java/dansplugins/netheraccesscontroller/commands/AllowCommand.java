package dansplugins.netheraccesscontroller.commands;

import dansplugins.netheraccesscontroller.PersistentData;
import dansplugins.netheraccesscontroller.utils.UUIDChecker;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class AllowCommand {

    public boolean execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /nac allow (playerName)");
            return false;
        }
        String playerName = args[0];
        UUID uuid = UUIDChecker.getInstance().findUUIDBasedOnPlayerName(playerName);

        if (uuid == null) {
            sender.sendMessage(ChatColor.RED + "That player wasn't found.");
            return false;
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);

        if (PersistentData.getInstance().isPlayerAllowed(uuid)) {
            sender.sendMessage(ChatColor.RED + player.getName() + " is already allowed to use nether portals.");
            return false;
        }

        PersistentData.getInstance().setPlayerAllowed(uuid, true);
        sender.sendMessage(ChatColor.GREEN + player.getName() + " is now allowed to use nether portals.");
        return true;
    }

}
