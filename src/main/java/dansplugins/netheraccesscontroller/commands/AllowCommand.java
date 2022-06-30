package dansplugins.netheraccesscontroller.commands;

import dansplugins.netheraccesscontroller.data.PersistentData;
import dansplugins.netheraccesscontroller.utils.UUIDChecker;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.UUID;

public class AllowCommand {
    private final UUIDChecker uuidChecker;
    private final PersistentData persistentData;

    public AllowCommand(UUIDChecker uuidChecker, PersistentData persistentData) {
        this.uuidChecker = uuidChecker;
        this.persistentData = persistentData;
    }

    public boolean execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /nac allow (playerName)");
            return false;
        }
        String playerName = args[0];
        UUID uuid = uuidChecker.findUUIDBasedOnPlayerName(playerName);

        if (uuid == null) {
            sender.sendMessage(ChatColor.RED + "That player wasn't found.");
            return false;
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);

        if (persistentData.isPlayerAllowed(uuid)) {
            sender.sendMessage(ChatColor.RED + player.getName() + " is already allowed to use nether portals.");
            return false;
        }

        persistentData.setPlayerAllowed(uuid, true);
        sender.sendMessage(ChatColor.GREEN + player.getName() + " is now allowed to use nether portals.");
        return true;
    }

}
