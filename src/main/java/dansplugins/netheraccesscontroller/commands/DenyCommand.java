package dansplugins.netheraccesscontroller.commands;

import dansplugins.netheraccesscontroller.PersistentData;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DenyCommand {

    public boolean execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            return false;
        }
        String playerName = args[0];
        Player player = Bukkit.getPlayer(playerName);
        PersistentData.getInstance().setPlayerAllowed(player, false);
        return true;
    }

}
