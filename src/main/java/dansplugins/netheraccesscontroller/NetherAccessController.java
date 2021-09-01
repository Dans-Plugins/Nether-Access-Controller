package dansplugins.netheraccesscontroller;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class NetherAccessController extends JavaPlugin implements Listener {

    private static NetherAccessController instance;

    public static NetherAccessController getInstance() {
        return instance;
    }

    private final boolean debug = true;

    private final String version = "v0.1";

    @Override
    public void onEnable() {
        // set instance
        instance = this;

        // register event handlers
        EventRegistry.getInstance().registerEvents();
    }

    @Override
    public void onDisable() {

    }

    public String getVersion() {
        return version;
    }

    public boolean isDebugEnabled() {
        return debug;
    }

    private boolean isVersionMismatched() {
        return !getConfig().getString("version").equalsIgnoreCase(getVersion());
    }
}
