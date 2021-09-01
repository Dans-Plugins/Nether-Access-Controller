package dansplugins.netheraccesscontroller;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class NetherAccessController extends JavaPlugin implements Listener {

    private final boolean debug = true;

    @Override
    public void onEnable() {
        PluginManager manager = this.getServer().getPluginManager();

        // event handlers
        manager.registerEvents(this, this);
    }

    @Override
    public void onDisable() {

    }

    @EventHandler()
    public void handle(PlayerPortalEvent event) {
        if (debug) {
            System.out.println("[DEBUG] PlayerPortalEvent is firing.");
            System.out.println("[DEBUG] " + event.getPlayer().getName() + " is using a portal.");
        }
        if (event.getPlayer().getName().equalsIgnoreCase("DanTheTechMan")) {
            event.getPlayer().sendMessage("DanTheTechMan is not allowed through nether portals.");
            event.setCancelled(true);
        }
    }

    @EventHandler()
    public void handle(PortalCreateEvent event) {
        if (debug) {
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
