package dansplugins.netheraccesscontroller.listeners;

import dansplugins.netheraccesscontroller.NetherAccessController;
import dansplugins.netheraccesscontroller.data.PersistentData;
import dansplugins.netheraccesscontroller.services.ConfigService;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerPortalEventListener implements Listener {
    private final NetherAccessController netherAccessController;
    private final ConfigService configService;
    private final PersistentData persistentData;

    public PlayerPortalEventListener(NetherAccessController netherAccessController, ConfigService configService, PersistentData persistentData) {
        this.netherAccessController = netherAccessController;
        this.configService = configService;
        this.persistentData = persistentData;
    }

    @EventHandler()
    public void handle(PlayerPortalEvent event) {
        if (netherAccessController.isDebugEnabled()) {
            System.out.println("[DEBUG] PlayerPortalEvent is firing.");
            System.out.println("[DEBUG] " + event.getPlayer().getName() + " is using a portal.");
        }

        if (!configService.getBoolean("preventPortalUsage")) {
            return;
        }

        if (!event.getCause().equals(PlayerTeleportEvent.TeleportCause.NETHER_PORTAL)) {
            return;
        }

        if (!persistentData.isPlayerAllowed(event.getPlayer())) {
            event.getPlayer().sendMessage(ChatColor.RED + configService.getString("denyUsageMessage"));
            event.setCancelled(true);
            return;
        }
        event.getPlayer().sendMessage(ChatColor.GREEN + "You step through the portal.");
    }

}
