package dansplugins.netheraccesscontroller;

import dansplugins.netheraccesscontroller.bstats.Metrics;
import dansplugins.netheraccesscontroller.data.PersistentData;
import dansplugins.netheraccesscontroller.services.CommandService;
import dansplugins.netheraccesscontroller.services.ConfigService;
import dansplugins.netheraccesscontroller.services.StorageService;
import dansplugins.netheraccesscontroller.utils.ArgumentParser;
import dansplugins.netheraccesscontroller.utils.EventRegistry;
import dansplugins.netheraccesscontroller.utils.UUIDChecker;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * @author Daniel McCoy Stephenson
 */
public final class NetherAccessController extends JavaPlugin implements Listener {
    private final String pluginVersion = "v" + getDescription().getVersion();

    private final ConfigService configService = new ConfigService(this);
    private final PersistentData persistentData = new PersistentData();
    private final EventRegistry eventRegistry = new EventRegistry(this, configService, persistentData);
    private final StorageService storageService = new StorageService(configService, this, persistentData);
    private final UUIDChecker uuidChecker = new UUIDChecker();
    private final ArgumentParser argumentParser = new ArgumentParser();

    @Override
    public void onEnable() {
        // create/load config
        if (!(new File("./plugins/NetherAccessController/config.yml").exists())) {
            configService.saveMissingConfigDefaultsIfNotPresent();
        }
        else {
            // pre load compatibility checks
            if (isVersionMismatched()) {
                configService.saveMissingConfigDefaultsIfNotPresent();
            }
            reloadConfig();
        }

        // register event handlers
        eventRegistry.registerEvents();

        // load save files
        storageService.load();

        // bStats
        int pluginId = 12673;
        Metrics metrics = new Metrics(this, pluginId);
    }

    @Override
    public void onDisable() {
        storageService.save();
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        CommandService commandService = new CommandService(this, persistentData, uuidChecker, configService, argumentParser);
        return commandService.interpretCommand(sender, label, args);
    }

    public String getVersion() {
        return pluginVersion;
    }

    public boolean isDebugEnabled() {
        return getConfig().getBoolean("debugMode");
    }

    private boolean isVersionMismatched() {
        return !getConfig().getString("version").equalsIgnoreCase(getVersion());
    }
}