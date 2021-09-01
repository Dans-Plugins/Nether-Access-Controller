package dansplugins.netheraccesscontroller.commands;

import dansplugins.netheraccesscontroller.PersistentData;
import dansplugins.netheraccesscontroller.utils.UUIDChecker;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class DenyCommand {

    public boolean execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            return false;
        }
        String playerName = args[0];
        UUID uuid = UUIDChecker.getInstance().findUUIDBasedOnPlayerName(playerName);

        if (uuid == null) {
            sender.sendMessage(ChatColor.RED + "That player wasn't found.");
            return false;
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);

        if (!PersistentData.getInstance().isPlayerAllowed(uuid)) {
            sender.sendMessage(ChatColor.RED + player.getName() + " is already not allowed to use nether portals.");
            return false;
        }

        PersistentData.getInstance().setPlayerAllowed(uuid, false);
        sender.sendMessage(ChatColor.GREEN + player.getName() + " is no longer allowed to use nether portals.");
        return true;
    }

}
