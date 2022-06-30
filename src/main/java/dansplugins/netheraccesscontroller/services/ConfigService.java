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

            if (option.equalsIgnoreCase("version")) {
                sender.sendMessage(ChatColor.RED + "Cannot set version.");
                return;
            } else if (option.equalsIgnoreCase("a")) {
                getConfig().set(option, Integer.parseInt(value));
                sender.sendMessage(ChatColor.GREEN + "Integer set.");
            } else if (option.equalsIgnoreCase("debugMode")
                    || option.equalsIgnoreCase("preventPortalUsage")
                    || option.equalsIgnoreCase("preventPortalCreation")) {
                getConfig().set(option, Boolean.parseBoolean(value));
                sender.sendMessage(ChatColor.GREEN + "Boolean set.");
            } else if (option.equalsIgnoreCase("c")) { // no doubles yet
                getConfig().set(option, Double.parseDouble(value));
                sender.sendMessage(ChatColor.GREEN + "Double set.");
            } else {
                getConfig().set(option, value);
                sender.sendMessage(ChatColor.GREEN + "String set.");
            }

            // save
            netherAccessController.saveConfig();
            altered = true;
        } else {
            sender.sendMessage(ChatColor.RED + "That config option wasn't found.");
        }
    }

    public void sendConfigList(CommandSender sender) {
        sender.sendMessage(ChatColor.AQUA + "=== Config List ===");
        sender.sendMessage(ChatColor.AQUA + "version: " + getConfig().getString("version")
                + ", debugMode: " + getString("debugMode")
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