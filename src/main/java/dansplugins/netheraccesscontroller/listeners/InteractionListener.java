package dansplugins.netheraccesscontroller.listeners;

import dansplugins.netheraccesscontroller.NetherAccessController;
import dansplugins.netheraccesscontroller.data.PersistentData;
import dansplugins.netheraccesscontroller.services.ConfigService;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * @author Daniel McCoy Stephenson
 */
public class InteractionListener implements Listener {
    private final NetherAccessController netherAccessController;
    private final ConfigService configService;
    private final PersistentData persistentData;

    public InteractionListener(NetherAccessController netherAccessController, ConfigService configService, PersistentData persistentData) {
        this.netherAccessController = netherAccessController;
        this.configService = configService;
        this.persistentData = persistentData;
    }

    @EventHandler()
    public void handle(PlayerInteractEvent event) {

        if (netherAccessController.isDebugEnabled()) {
            System.out.println("[DEBUG] PlayerInteractEvent is firing.");
            System.out.println("[DEBUG] " + event.getPlayer().getName() + " is interacting.");
        }

        if (!configService.getBoolean("preventPortalCreation")) {
            return;
        }

        Block clickedBlock = event.getClickedBlock();

        if (clickedBlock == null) {
            return;
        }

        if (clickedBlock.getBlockData().getMaterial() != Material.OBSIDIAN) {
            return;
        }

        ItemStack itemInMainHand = event.getPlayer().getInventory().getItemInMainHand();
        ItemStack itemInOffHand = event.getPlayer().getInventory().getItemInOffHand();

        if (itemInMainHand.getType() != Material.FLINT_AND_STEEL && itemInOffHand.getType() != Material.FLINT_AND_STEEL) {
            return;
        }

        if (persistentData.isPlayerAllowed(event.getPlayer())) {
            event.getPlayer().sendMessage(ChatColor.GREEN + "You light the portal.");
        }
        else {
            event.getPlayer().sendMessage(ChatColor.RED + configService.getString("denyCreationMessage"));
            event.setCancelled(true);
        }

    }
}
