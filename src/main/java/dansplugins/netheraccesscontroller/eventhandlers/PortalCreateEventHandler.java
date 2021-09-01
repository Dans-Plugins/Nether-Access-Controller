package dansplugins.netheraccesscontroller.eventhandlers;

import dansplugins.netheraccesscontroller.NetherAccessController;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.PortalCreateEvent;

public class PortalCreateEventHandler implements Listener {

    @EventHandler()
    public void handle(PortalCreateEvent event) {
        if (NetherAccessController.getInstance().isDebugEnabled()) {
            System.out.println("[DEBUG] PortalCreateEvent is firing.");
            Player player = (Player) event.getEntity();
            if (event.getEntity() != null) {
                System.out.println("[DEBUG] " + event.getEntity().getName() + " is creating a portal.");
            }
            else {
                System.out.println("[DEBUG] Creator of the portal is null.");
            }
            if (player != null) {
                System.out.println("[DEBUG] Entity was cast to Player for " + player.getName());
            }
            else {
                System.out.println("[DEBUG] Entity was cast to Player but remained null.");
            }
            System.out.println("[DEBUG] " + event.getBlocks().size() + " blocks were used.");
        }
    }

}
