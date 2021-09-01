package dansplugins.netheraccesscontroller;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class NetherAccessController extends JavaPlugin implements Listener {

    private static NetherAccessController instance;

    public static NetherAccessController getInstance() {
        return instance;
    }

    private final boolean debug = true;

    private final String version = "v0.2";

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

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        CommandInterpreter commandInterpreter = new CommandInterpreter();
        return commandInterpreter.interpretCommand(sender, label, args);
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
