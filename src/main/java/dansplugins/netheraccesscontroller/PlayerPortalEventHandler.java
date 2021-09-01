package dansplugins.netheraccesscontroller;

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
        if (event.getPlayer().getName().equalsIgnoreCase("DanTheTechMan")) {
            event.getPlayer().sendMessage("DanTheTechMan is not allowed through nether portals.");
            event.setCancelled(true);
        }
    }

}
