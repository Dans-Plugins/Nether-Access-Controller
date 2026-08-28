package dansplugins.netheraccesscontroller.services;

import dansplugins.netheraccesscontroller.NetherAccessController;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

/*
    To add a new config option, the following methods must be altered:
    - saveMissingConfigDefaultsIfNotPresent
    - setConfigOption()
    - sendConfigList()
 */

/**
 * @author Daniel McCoy Stephenson
 */
public class ConfigService {
    private final NetherAccessController netherAccessController;

    private boolean altered = false;

    public ConfigService(NetherAccessController netherAccessController) {
        this.netherAccessController = netherAccessController;
    }

    public void saveMissingConfigDefaultsIfNotPresent() {
        // set version
        if (!getConfig().isString("version")) {
            getConfig().addDefault("version", netherAccessController.getVersion());
        }
        else {
            getConfig().set("version", netherAccessController.getVersion());
        }

        // save config options
        if (!getConfig().isSet("debugMode")) {
            getConfig().set("debugMode", false);
        }
        if (!getConfig().isSet("denyUsageMessage")) {
            getConfig().set("denyUsageMessage", "You're unable to use nether portals.");
        }
        if (!getConfig().isSet("denyCreationMessage")) {
            getConfig().set("denyCreationMessage", "You're unable to create nether portals.");
        }
        if (!getConfig().isSet("preventPortalUsage")) {
            getConfig().set("preventPortalUsage", false);
        }
        if (!getConfig().isSet("preventPortalCreation")) {
            getConfig().set("preventPortalCreation", true);
        }
        getConfig().options().copyDefaults(true);
        netherAccessController.saveConfig();
    }

    public void setConfigOption(String option, String value, CommandSender sender) {

        if (getConfig().isSet(option)) {

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
                || option.equals("preventPortalCreation");
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
                + ", denyCreationMessage: '" + getString("denyCreationMessage") + "'");
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

    public int getInt(String option) {
        return getConfig().getInt(option);
    }

    public boolean getBoolean(String option) {
        return getConfig().getBoolean(option);
    }

    public double getDouble(String option) {
        return getConfig().getDouble(option);
    }

    public String getString(String option) {
        return getConfig().getString(option);
    }

}