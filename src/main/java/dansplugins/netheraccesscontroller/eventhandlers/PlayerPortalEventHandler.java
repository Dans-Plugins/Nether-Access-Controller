package dansplugins.netheraccesscontroller.eventhandlers;

import dansplugins.netheraccesscontroller.NetherAccessController;
import dansplugins.netheraccesscontroller.data.PersistentData;
import dansplugins.netheraccesscontroller.managers.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;

public class PlayerPortalEventHandler implements Listener {

    @EventHandler()
    public void handle(PlayerPortalEvent event) {
        if (NetherAccessController.getInstance().isDebugEnabled()) {
            System.out.println("[DEBUG] PlayerPortalEvent is firing.");
            System.out.println("[DEBUG] " + event.getPlayer().getName() + " is using a portal.");
        }

        if (!PersistentData.getInstance().isPlayerAllowed(event.getPlayer())) {
            event.getPlayer().sendMessage(ChatColor.RED + ConfigManager.getInstance().getString("denyMessage"));
            event.setCancelled(true);
            return;
        }
        event.getPlayer().sendMessage(ChatColor.GREEN + "You step through the portal.");
    }

}
