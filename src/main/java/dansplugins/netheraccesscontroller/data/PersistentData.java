package dansplugins.netheraccesscontroller.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
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
import java.util.Objects;
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
        setPlayerAllowed(player.getUniqueId(), value);
    }

    /**
     * Every copy of an entry is removed, not only the first: a hand edit of the save file can list
     * a player twice, and a copy left behind keeps them allowed after /nac deny says they are not.
     */
    public void setPlayerAllowed(UUID playerUUID, boolean value) {
        if (value) {
            allowedPlayers.add(playerUUID);
        }
        else {
            allowedPlayers.removeIf(playerUUID::equals);
        }
    }

    /** Header printed above the whitelist by {@code /nac list}. */
    static final String LIST_HEADER = "=== Players With Access To The Nether ===";

    public void sendListToSender(CommandSender sender) {
        if (allowedPlayers.size() > 0) {
            sender.sendMessage(ChatColor.AQUA + LIST_HEADER);
            for (UUID uuid : allowedPlayers) {
                OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
                String name = player.getName();
                sender.sendMessage(ChatColor.AQUA + (name != null ? name : uuid.toString()));
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

    /**
     * Replaces the whitelist with the one described by the given save data.
     *
     * Save data that carries no usable whitelist leaves an empty whitelist rather than a null
     * one. A null whitelist would make every later access check throw out of its event handler,
     * which leaves the restricted event uncancelled and lets the player through; an empty
     * whitelist denies everyone, which is the safe direction for this plugin. A blank entry, which
     * is what a comma left behind by a hand edit reads as, is dropped for the same reason.
     *
     * Returns false if no whitelist could be recovered, so that the caller can keep what the save
     * data was read from rather than let the next save replace it with the empty whitelist.
     */
    public boolean load(Map<String, String> data) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        Type arrayListTypeUUID = new TypeToken<ArrayList<UUID>>(){}.getType();

        ArrayList<UUID> loadedPlayers = null;
        if (data != null) {
            try {
                // Gson returns null when the key is absent or its value is the JSON literal null.
                loadedPlayers = gson.fromJson(data.get("allowedPlayers"), arrayListTypeUUID);
            } catch (JsonParseException | IllegalArgumentException e) {
                // Which of the two an invalid UUID raises depends on the Gson the server provides.
                System.out.println("ERROR: Saved whitelist could not be parsed: " + e.toString());
            }
        }

        if (loadedPlayers == null) {
            System.out.println("WARNING: No whitelist could be recovered from the save data. "
                    + "No player is allowed to access the nether until one is allowed again.");
            allowedPlayers = new ArrayList<UUID>();
            return false;
        }

        if (loadedPlayers.removeIf(Objects::isNull)) {
            System.out.println("WARNING: Blank entries in the saved whitelist were ignored.");
        }

        allowedPlayers = loadedPlayers;
        return true;
    }
}
