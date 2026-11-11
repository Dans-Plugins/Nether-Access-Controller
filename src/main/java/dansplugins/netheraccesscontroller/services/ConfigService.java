package dansplugins.netheraccesscontroller.services;

import dansplugins.netheraccesscontroller.NetherAccessController;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/*
    To add a new config option, the following methods must be altered:
    - saveMissingConfigDefaultsIfNotPresent
    - setConfigOption()
    - sendConfigList()
    A boolean option must also be named in isBooleanOption(), or setConfigOption() will store it
    as a string and accept any value for it, and given its default in BOOLEAN_DEFAULTS, or a
    value in the file that is not a boolean will read as false.

    The usage-reporting block is the exception: it is bundled in src/main/resources/config.yml
    rather than written by saveMissingConfigDefaultsIfNotPresent, and read through the accessors
    at the bottom of this class. See the comment above them for why.
 */

/**
 * @author Daniel McCoy Stephenson
 */
public class ConfigService {
    private static final String USAGE_REPORTING_SECTION = "usage-reporting";
    private static final String USAGE_REPORTING_ENABLED_KEY = "usage-reporting.enabled";
    private static final String USAGE_REPORTING_ENDPOINT_KEY = "usage-reporting.endpoint";
    private static final String USAGE_REPORTING_KEY_KEY = "usage-reporting.key";
    private static final String DEFAULT_USAGE_REPORTING_ENDPOINT = "https://trace.danielstephenson.dev";

    /**
     * The boolean options saveMissingConfigDefaultsIfNotPresent writes, with the defaults it writes
     * them with. No default is registered for them with Bukkit, so its one-argument getBoolean
     * reads a value that is not a boolean (a typo, a quoted "true", a 1) as false. getBoolean reads
     * such a value as the default here instead, so that a bad hand edit cannot turn
     * preventPortalCreation off (#126). usage-reporting.enabled is not listed: the bundled
     * config.yml is registered as its default, so a bad value already reads as true.
     */
    private static final Map<String, Boolean> BOOLEAN_DEFAULTS;

    static {
        Map<String, Boolean> defaults = new LinkedHashMap<>();
        defaults.put("debugMode", false);
        defaults.put("preventPortalUsage", false);
        defaults.put("preventPortalCreation", true);
        BOOLEAN_DEFAULTS = Collections.unmodifiableMap(defaults);
    }

    private final NetherAccessController netherAccessController;

    private boolean altered = false;

    public ConfigService(NetherAccessController netherAccessController) {
        this.netherAccessController = netherAccessController;
    }

    public void saveMissingConfigDefaultsIfNotPresent() {
        // set version
        if (!getConfig().isString("version")) {
            getConfig().addDefault("version", getRunningVersion());
        }
        else {
            getConfig().set("version", getRunningVersion());
        }

        // save config options
        if (!getConfig().isSet("debugMode")) {
            getConfig().set("debugMode", BOOLEAN_DEFAULTS.get("debugMode"));
        }
        if (!getConfig().isSet("denyUsageMessage")) {
            getConfig().set("denyUsageMessage", "You're unable to use nether portals.");
        }
        if (!getConfig().isSet("denyCreationMessage")) {
            getConfig().set("denyCreationMessage", "You're unable to create nether portals.");
        }
        if (!getConfig().isSet("preventPortalUsage")) {
            getConfig().set("preventPortalUsage", BOOLEAN_DEFAULTS.get("preventPortalUsage"));
        }
        if (!getConfig().isSet("preventPortalCreation")) {
            getConfig().set("preventPortalCreation", BOOLEAN_DEFAULTS.get("preventPortalCreation"));
        }
        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    public void setConfigOption(String option, String value, CommandSender sender) {

        // A section is not an option. Setting usage-reporting itself would replace the block with
        // a string, which turns a usage-reporting.enabled: false back on through the bundled
        // defaults and leaves that option impossible to set.
        if (getConfig().isSet(option) && !getConfig().isConfigurationSection(option)) {

            if (option.equals("version")) {
                sender.sendMessage(ChatColor.RED + "Cannot set version.");
                return;
            } else if (isBooleanOption(option)) {
                Boolean parsedValue = parseBoolean(value);
                if (parsedValue == null) {
                    sender.sendMessage(ChatColor.RED + "'" + value + "' isn't a valid value for "
                            + option + ". Accepted values are 'true' and 'false'.");
                    return;
                }
                getConfig().set(option, parsedValue);
                sender.sendMessage(ChatColor.GREEN + "Boolean set.");
            } else {
                getConfig().set(option, value);
                sender.sendMessage(ChatColor.GREEN + "String set.");
            }

            // save
            saveConfig();
            altered = true;

            if (option.startsWith(USAGE_REPORTING_SECTION)) {
                applyUsageReportingConfig();
            }
        } else {
            sender.sendMessage(ChatColor.RED + "That config option wasn't found.");
        }
    }

    /**
     * The options that saveMissingConfigDefaultsIfNotPresent writes as booleans. The comparison is
     * exact because the isSet gate that precedes it is: a differently-cased spelling of an option
     * name is rejected there first, so accepting one here would never be reached.
     */
    private boolean isBooleanOption(String option) {
        return option.equals("debugMode")
                || option.equals("preventPortalUsage")
                || option.equals("preventPortalCreation")
                || option.equals(USAGE_REPORTING_ENABLED_KEY);
    }

    /**
     * Returns null for any value that is neither 'true' nor 'false', so that the caller can reject
     * it. Boolean.parseBoolean cannot be used here: it reports every unrecognised value as false
     * without error, which turns a typo in an enforcement option into a silent disabling of it.
     */
    private Boolean parseBoolean(String value) {
        if (value.equalsIgnoreCase("true")) {
            return true;
        }
        if (value.equalsIgnoreCase("false")) {
            return false;
        }
        return null;
    }

    public void sendConfigList(CommandSender sender) {
        sender.sendMessage(ChatColor.AQUA + "=== Config List ===");
        sender.sendMessage(ChatColor.AQUA + "version: " + getConfig().getString("version")
                + ", debugMode: " + getBoolean("debugMode")
                + ", preventPortalUsage: " + getBoolean("preventPortalUsage")
                + ", preventPortalCreation: " + getBoolean("preventPortalCreation")
                + ", denyUsageMessage: '" + getString("denyUsageMessage") + "'"
                + ", denyCreationMessage: '" + getString("denyCreationMessage") + "'"
                + ", usage-reporting.enabled: " + isUsageReportingEnabled());
    }

    public boolean hasBeenAltered() {
        return altered;
    }

    public FileConfiguration getConfig() {
        return netherAccessController.getConfig();
    }

    /**
     * Package-private rather than public so that a test can supply an in-memory configuration in
     * place of a live plugin; nothing outside this package needs to save the config directly.
     */
    void saveConfig() {
        netherAccessController.saveConfig();
    }

    /**
     * The running usage-reporting client reads its settings once, so it is replaced after a change
     * to one of them. Package-private for the same reason as saveConfig: a test records the call
     * in place of a live plugin.
     */
    void applyUsageReportingConfig() {
        netherAccessController.applyUsageReportingConfig();
    }

    /**
     * The version saveMissingConfigDefaultsIfNotPresent records in the file. Package-private for
     * the same reason as saveConfig: a test supplies it in place of a live plugin.
     */
    String getRunningVersion() {
        return netherAccessController.getVersion();
    }

    public int getInt(String option) {
        return getConfig().getInt(option);
    }

    /**
     * For an option in BOOLEAN_DEFAULTS whose value is missing or is not a boolean, returns that
     * option's documented default rather than the false Bukkit would return.
     */
    public boolean getBoolean(String option) {
        Boolean fallback = BOOLEAN_DEFAULTS.get(option);
        if (fallback != null && !getConfig().isBoolean(option)) {
            return fallback;
        }
        return getConfig().getBoolean(option);
    }

    /**
     * Logs a warning for each option in BOOLEAN_DEFAULTS whose value in config.yml is not a
     * boolean, naming the value found and the default used in its place. Called once the config
     * has been loaded on enable; nothing else reports a bad hand edit.
     */
    public void warnAboutNonBooleanValues() {
        for (Map.Entry<String, Boolean> entry : BOOLEAN_DEFAULTS.entrySet()) {
            String option = entry.getKey();
            if (getConfig().isSet(option) && !getConfig().isBoolean(option)) {
                logWarning("config.yml has '" + getConfig().get(option) + "' for " + option
                        + ", which is not true or false. Using the default, " + entry.getValue()
                        + ", until it is corrected with /nac config set " + option
                        + " <true|false> or by editing the file.");
            }
        }
    }

    /**
     * Package-private for the same reason as saveConfig: a test records the warning in place of a
     * live plugin's logger.
     */
    void logWarning(String message) {
        netherAccessController.getLogger().warning(message);
    }

    public double getDouble(String option) {
        return getConfig().getDouble(option);
    }

    public String getString(String option) {
        return getConfig().getString(option);
    }

    // The one-argument getters, deliberately. saveDefaultConfig() never touches a
    // config.yml that already exists, and saveMissingConfigDefaultsIfNotPresent only
    // runs on a version change, so a server upgraded from a version before usage
    // reporting can have no usage-reporting block on disk. Bukkit registers the
    // jar's config.yml as the defaults for that file, and the one-argument
    // getters fall through to them -- but the two-argument getters return their
    // explicit fallback instead, which for the key would be "" and would turn
    // reporting off on every existing installation. Verified against
    // YamlConfiguration, not assumed.

    public boolean isUsageReportingEnabled() {
        return getConfig().getBoolean(USAGE_REPORTING_ENABLED_KEY);
    }

    public String getUsageReportingEndpoint() {
        String endpoint = getConfig().getString(USAGE_REPORTING_ENDPOINT_KEY);
        return endpoint != null ? endpoint : DEFAULT_USAGE_REPORTING_ENDPOINT;
    }

    /** Empty when no key is configured or bundled, which the client treats as "off". */
    public String getUsageReportingKey() {
        String key = getConfig().getString(USAGE_REPORTING_KEY_KEY);
        return key != null ? key : "";
    }

}