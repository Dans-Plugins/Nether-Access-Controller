package dansplugins.netheraccesscontroller.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Daniel McCoy Stephenson
 */
public class PersistentData {
    private ArrayList<UUID> allowedPlayers = new ArrayList<UUID>();

    public ArrayList<UUID> getAllowedPlayers() {
        return allowedPlayers;
    }

    public boolean isPlayerAllowed(Player player) {
        for (UUID uuid : allowedPlayers) {
            if (uuid.equals(player.getUniqueId())) {
                return true;
            }
        }
        return false;
    }

    public boolean isPlayerAllowed(UUID playerUUID) {
        for (UUID uuid : allowedPlayers) {
            if (uuid.equals(playerUUID)) {
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

    public void setPlayerAllowed(UUID playerUUID, boolean value) {
        if (value) {
            allowedPlayers.add(playerUUID);
        }
        else {
            allowedPlayers.remove(playerUUID);
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

    public Map<String, String> save() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();;

        Map<String, String> saveMap = new HashMap<>();
        saveMap.put("allowedPlayers", gson.toJson(allowedPlayers));

        return saveMap;
    }

    public void load(Map<String, String> data) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        Type arrayListTypeUUID = new TypeToken<ArrayList<UUID>>(){}.getType();

        allowedPlayers = gson.fromJson(data.get("allowedPlayers"), arrayListTypeUUID);
    }
}
