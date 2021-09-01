package dansplugins.netheraccesscontroller;

import dansplugins.netheraccesscontroller.managers.ConfigManager;
import dansplugins.netheraccesscontroller.managers.StorageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class NetherAccessController extends JavaPlugin implements Listener {

    private static NetherAccessController instance;

    public static NetherAccessController getInstance() {
        return instance;
    }

    private final String version = "v0.5";

    @Override
    public void onEnable() {
        // set instance
        instance = this;

        // create/load config
        if (!(new File("./plugins/NetherAccessController/config.yml").exists())) {
            ConfigManager.getInstance().saveMissingConfigDefaultsIfNotPresent();
        }
        else {
            // pre load compatibility checks
            if (isVersionMismatched()) {
                ConfigManager.getInstance().saveMissingConfigDefaultsIfNotPresent();
            }
            reloadConfig();
        }

        // load save files
        StorageManager.getInstance().load();

        // register event handlers
        EventRegistry.getInstance().registerEvents();
    }

    @Override
    public void onDisable() {
        StorageManager.getInstance().save();
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        CommandInterpreter commandInterpreter = new CommandInterpreter();
        return commandInterpreter.interpretCommand(sender, label, args);
    }

    public String getVersion() {
        return version;
    }

    public boolean isDebugEnabled() {
        return getConfig().getBoolean("debugMode");
    }

    private boolean isVersionMismatched() {
        return !getConfig().getString("version").equalsIgnoreCase(getVersion());
    }
}
