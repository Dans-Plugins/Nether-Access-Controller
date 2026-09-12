package dansplugins.netheraccesscontroller;

import dansplugins.netheraccesscontroller.bstats.Metrics;
import dansplugins.netheraccesscontroller.data.PersistentData;
import dansplugins.netheraccesscontroller.services.CommandService;
import dansplugins.netheraccesscontroller.services.ConfigService;
import dansplugins.netheraccesscontroller.services.StorageService;
import dansplugins.netheraccesscontroller.trace.TraceClient;
import dansplugins.netheraccesscontroller.utils.ArgumentParser;
import dansplugins.netheraccesscontroller.utils.EventRegistry;
import dansplugins.netheraccesscontroller.utils.UUIDChecker;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;

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

    // A no-op until the config has been read, so a command arriving before
    // onEnable() finishes has something safe to report to.
    private TraceClient trace = TraceClient.disabled();

    @Override
    public void onEnable() {
        // create/load config. The bundled config.yml carries only the usage-reporting block; on
        // first run it is written out with its comments, and on every run it is registered as the
        // defaults that the one-argument config getters fall through to. saveDefaultConfig() never
        // touches a file that already exists. A first run then reads as a version mismatch (the
        // file has no version key), which is the case that writes the remaining defaults.
        saveDefaultConfig();
        if (isVersionMismatched()) {
            configService.saveMissingConfigDefaultsIfNotPresent();
        }
        else {
            reloadConfig();
        }

        // register event handlers
        eventRegistry.registerEvents();

        // load save files
        storageService.load();

        // bStats
        int pluginId = 12673;
        Metrics metrics = new Metrics(this, pluginId);

        // usage reporting: one event now, one per command; see config.yml
        trace = TraceClient.builder(configService.getUsageReportingEndpoint(), getName())
                .key(configService.getUsageReportingKey())
                .enabled(configService.isUsageReportingEnabled())
                .logger(getLogger())
                .build();
        trace.report("startup", null, Collections.singletonMap("version", getDescription().getVersion()));
    }

    @Override
    public void onDisable() {
        trace.close();
        storageService.save();
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        trace.report("command", null, Collections.singletonMap("name", cmd.getName()));
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
        return isVersionMismatched(getConfig().getString("version"), getVersion());
    }

    /**
     * Reports whether the version recorded in the config file differs from the running version.
     *
     * A config file with no version key at all counts as mismatched, which routes the caller to
     * saveMissingConfigDefaultsIfNotPresent and writes the key back. Treating it as anything else
     * would mean throwing out of onEnable, and a plugin that fails to enable registers no
     * listeners, so every player could then create and use nether portals.
     *
     * Package-private and static so that this can be exercised without a running server.
     */
    static boolean isVersionMismatched(String configVersion, String pluginVersion) {
        return configVersion == null || !configVersion.equalsIgnoreCase(pluginVersion);
    }
}