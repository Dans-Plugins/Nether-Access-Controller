package dansplugins.netheraccesscontroller.utils;

import dansplugins.netheraccesscontroller.NetherAccessController;
import dansplugins.netheraccesscontroller.data.PersistentData;
import dansplugins.netheraccesscontroller.listeners.InteractionListener;
import dansplugins.netheraccesscontroller.listeners.PlayerPortalEventListener;
import dansplugins.netheraccesscontroller.services.ConfigService;
import org.bukkit.plugin.PluginManager;

/**
 * @author Daniel McCoy Stephenson
 */
public class EventRegistry {
    private final NetherAccessController netherAccessController;
    private final ConfigService configService;
    private final PersistentData persistentData;

    public EventRegistry(NetherAccessController netherAccessController, ConfigService configService, PersistentData persistentData) {
        this.netherAccessController = netherAccessController;
        this.configService = configService;
        this.persistentData = persistentData;
    }

    public void registerEvents() {

        NetherAccessController mainInstance = netherAccessController;
        PluginManager manager = mainInstance.getServer().getPluginManager();

        // event handlers
        manager.registerEvents(new PlayerPortalEventListener(netherAccessController, configService, persistentData), mainInstance);
        manager.registerEvents(new InteractionListener(netherAccessController, configService, persistentData), mainInstance);
    }

}
