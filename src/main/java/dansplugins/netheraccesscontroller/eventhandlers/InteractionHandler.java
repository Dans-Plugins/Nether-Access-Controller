package dansplugins.netheraccesscontroller.eventhandlers;

import dansplugins.netheraccesscontroller.NetherAccessController;
import dansplugins.netheraccesscontroller.data.PersistentData;
import dansplugins.netheraccesscontroller.managers.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class InteractionHandler implements Listener {

    @EventHandler()
    public void handle(PlayerInteractEvent event) {

        if (NetherAccessController.getInstance().isDebugEnabled()) {
            System.out.println("[DEBUG] PlayerInteractEvent is firing.");
            System.out.println("[DEBUG] " + event.getPlayer().getName() + " is interacting.");
        }

        if (!ConfigManager.getInstance().getBoolean("preventPortalCreation")) {
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

        if (PersistentData.getInstance().isPlayerAllowed(event.getPlayer())) {
            event.getPlayer().sendMessage(ChatColor.GREEN + "You light the portal.");
        }
        else {
            event.getPlayer().sendMessage(ChatColor.RED + ConfigManager.getInstance().getString("denyMessage"));
            event.setCancelled(true);
        }

    }
}
