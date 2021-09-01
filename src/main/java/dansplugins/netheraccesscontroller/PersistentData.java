package dansplugins.netheraccesscontroller;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

public class PersistentData {
    private static PersistentData instance;

    ArrayList<UUID> allowedPlayers = new ArrayList<UUID>();

    private PersistentData() {

    }

    public static PersistentData getInstance() {
        if (instance == null) {
            instance = new PersistentData();
        }
        return instance;
    }

    public boolean isPlayerAllowed(Player player) {
        for (UUID uuid : allowedPlayers) {
            if (uuid.equals(player.getUniqueId())) {
                return true;
            }
        }
        return false;
    }

    public void setPlayerAllowed(Player player, boolean value) {
        if (value) {
            allowedPlayers.add(player.getUniqueId());
        }
        else {
            allowedPlayers.remove(player.getUniqueId());
        }
    }

    public void sendListToSender(CommandSender sender) {
        if (allowedPlayers.size() > 0) {
            sender.sendMessage(ChatColor.AQUA + "=== Players With Access To The Nether ==");
            for (UUID uuid : allowedPlayers) {
                OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
                sender.sendMessage(ChatColor.AQUA + player.getName());
            }
        }
        else {
            sender.sendMessage(ChatColor.AQUA + "No one is allowed to access the nether.");
        }
    }
}
