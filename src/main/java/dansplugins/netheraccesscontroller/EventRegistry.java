package dansplugins.netheraccesscontroller;

import dansplugins.netheraccesscontroller.eventhandlers.InteractionHandler;
import dansplugins.netheraccesscontroller.eventhandlers.PlayerPortalEventHandler;
import dansplugins.netheraccesscontroller.eventhandlers.PortalCreateEventHandler;
import org.bukkit.plugin.PluginManager;

public class EventRegistry {

    private static EventRegistry instance;

    private EventRegistry() {

    }

    public static EventRegistry getInstance() {
        if (instance == null) {
            instance = new EventRegistry();
        }
        return instance;
    }

    public void registerEvents() {

        NetherAccessController mainInstance = NetherAccessController.getInstance();
        PluginManager manager = mainInstance.getServer().getPluginManager();

        // event handlers
        manager.registerEvents(new PlayerPortalEventHandler(), mainInstance);
        manager.registerEvents(new PortalCreateEventHandler(), mainInstance);
        manager.registerEvents(new InteractionHandler(), mainInstance);
    }

}
