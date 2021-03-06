package dansplugins.netheraccesscontroller.services;

import dansplugins.netheraccesscontroller.NetherAccessController;
import dansplugins.netheraccesscontroller.commands.*;
import dansplugins.netheraccesscontroller.data.PersistentData;
import dansplugins.netheraccesscontroller.utils.ArgumentParser;
import dansplugins.netheraccesscontroller.utils.UUIDChecker;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 * @author Daniel McCoy Stephenson
 */
public class CommandService {
    private final NetherAccessController netherAccessController;
    private final PersistentData persistentData;
    private final UUIDChecker uuidChecker;
    private final ConfigService configService;
    private final ArgumentParser argumentParser;

    public CommandService(NetherAccessController netherAccessController, PersistentData persistentData, UUIDChecker uuidChecker, ConfigService configService, ArgumentParser argumentParser) {
        this.netherAccessController = netherAccessController;
        this.persistentData = persistentData;
        this.uuidChecker = uuidChecker;
        this.configService = configService;
        this.argumentParser = argumentParser;
    }

    public boolean interpretCommand(CommandSender sender, String label, String[] args) {
        if (label.equalsIgnoreCase("NetherAccessController") || label.equalsIgnoreCase("nac")) {

            if (args.length == 0) {
                sender.sendMessage(ChatColor.AQUA + "Nether Access Controller " + netherAccessController.getVersion());
                sender.sendMessage(ChatColor.AQUA + "Developer: DanTheTechMan");
                sender.sendMessage(ChatColor.AQUA + "Wiki: https://github.com/dmccoystephenson/Nether-Access-Controller/wiki");
                return false;
            }

            String secondaryLabel = args[0];
            String[] arguments = getArguments(args);

            if (secondaryLabel.equalsIgnoreCase("help")) {
                if (!checkPermission(sender, "nac.help")) { return false; }
                HelpCommand command = new HelpCommand();
                return command.execute(sender);
            }

            if (secondaryLabel.equalsIgnoreCase("list")) {
                if (!checkPermission(sender, "nac.list")) { return false; }
                ListCommand command = new ListCommand(persistentData);
                return command.execute(sender);
            }

            if (secondaryLabel.equalsIgnoreCase("allow")) {
                if (!checkPermission(sender, "nac.allow")) { return false; }
                AllowCommand command = new AllowCommand(uuidChecker, persistentData);
                return command.execute(sender, arguments);
            }

            if (secondaryLabel.equalsIgnoreCase("deny")) {
                if (!checkPermission(sender, "nac.deny")) { return false; }
                DenyCommand command = new DenyCommand(uuidChecker, persistentData);
                return command.execute(sender, arguments);
            }

            if (secondaryLabel.equalsIgnoreCase("config")) {
                checkPermission(sender, "nac.config");
                ConfigCommand command = new ConfigCommand(configService, argumentParser);
                return command.execute(sender, arguments);
            }

        }

        sender.sendMessage(ChatColor.RED + "Nether Access Controller doesn't recognize that command.");
        return false;
    }

    private String[] getArguments(String[] args) {
        String[] toReturn = new String[args.length - 1];

        for (int i = 1; i < args.length; i++) {
            toReturn[i - 1] = args[i];
        }

        return toReturn;
    }

    private boolean checkPermission(CommandSender sender, String permission) {
        if (!sender.hasPermission(permission)) {
            sender.sendMessage(ChatColor.RED + "In order to use this command, you need the following permission: '" + permission + "'");
            return false;
        }
        return true;
    }

}
