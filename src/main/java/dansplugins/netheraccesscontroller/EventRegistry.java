package dansplugins.netheraccesscontroller;

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
    }

}
