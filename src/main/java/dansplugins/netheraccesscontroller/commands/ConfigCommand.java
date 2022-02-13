package dansplugins.netheraccesscontroller.commands;

import dansplugins.netheraccesscontroller.services.LocalConfigService;
import dansplugins.netheraccesscontroller.utils.ArgumentParser;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;

public class ConfigCommand {

    public boolean execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Sub-commands: show, set");
            return false;
        }

        if (args[0].equalsIgnoreCase("show")) {
            LocalConfigService.getInstance().sendConfigList(sender);
            return true;
        }
        else if (args[0].equalsIgnoreCase("set")) {
            if (args.length < 3) {
                sender.sendMessage(ChatColor.RED + "Usage: /nac config set (option) (value)");
                return false;
            }
            String option = args[1];

            String value = "";
            if (option.equalsIgnoreCase("denyUsageMessage") || option.equalsIgnoreCase("denyCreationMessage")) {
                ArrayList<String> singleQuoteArgs = ArgumentParser.getInstance().getArgumentsInsideSingleQuotes(args);
                if (singleQuoteArgs.size() == 0) {
                    sender.sendMessage(ChatColor.RED + "New message must be in between single quotes.");
                    return false;
                }
                value = singleQuoteArgs.get(0);
            }
            else {
                value = args[2];
            }

            LocalConfigService.getInstance().setConfigOption(option, value, sender);
            return true;
        }
        else {
            sender.sendMessage(ChatColor.RED + "Sub-commands: show, set");
            return false;
        }
    }

}
