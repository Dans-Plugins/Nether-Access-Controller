package dansplugins.netheraccesscontroller;

import dansplugins.netheraccesscontroller.commands.StubServer;
import dansplugins.netheraccesscontroller.listeners.ListenerFixtures;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the version comparison that onEnable uses to decide whether an existing config file
 * needs its defaults written back. The comparison runs before anything else in onEnable, so an
 * exception thrown here stops the plugin from enabling, and a plugin that does not enable
 * registers no listeners — every player can then create and use nether portals regardless of the
 * whitelist.
 *
 * The comparison is exercised through its static form; the instance form only supplies it with
 * the config value and the running version, both of which need a live Bukkit server to obtain.
 *
 * Also covers how onCommand hands a command on. Commands are sent through Bukkit's own
 * SimpleCommandMap, because the label that reaches onCommand is the map's choice: the one the
 * sender typed, which is not always the command's name.
 */
class NetherAccessControllerTest {

    @Test
    void isVersionMismatched_withMatchingVersions_reportsNoMismatch() {
        assertFalse(NetherAccessController.isVersionMismatched("v2.0.0", "v2.0.0"));
    }

    @Test
    void isVersionMismatched_ignoresCase() {
        assertFalse(NetherAccessController.isVersionMismatched("V2.0.0", "v2.0.0"));
    }

    @Test
    void isVersionMismatched_withDifferentVersions_reportsMismatch() {
        assertTrue(NetherAccessController.isVersionMismatched("v1.1.0", "v2.0.0"));
    }

    /**
     * A config file with no version key at all must be reported as mismatched rather than
     * dereferenced, so that the caller rewrites the defaults instead of aborting startup.
     */
    @Test
    void isVersionMismatched_withNoVersionRecorded_reportsMismatch() {
        assertTrue(NetherAccessController.isVersionMismatched(null, "v2.0.0"));
    }

    @Test
    void onCommand_acceptsTheCommandUnderItsOwnName() {
        List<String> messages = new ArrayList<>();

        dispatch("nac help", messages);

        assertEquals(6, messages.size());
        assertTrue(messages.get(0).contains("=== Nether Access Controller Commands ==="));
    }

    /**
     * The namespaced form is how a server reaches this command when another plugin has claimed
     * /nac, so it has to behave as /nac does.
     */
    @Test
    void onCommand_acceptsTheCommandUnderItsNamespacedLabel() {
        List<String> messages = new ArrayList<>();

        dispatch("netheraccesscontroller:nac help", messages);

        assertEquals(6, messages.size());
        assertTrue(messages.get(0).contains("=== Nether Access Controller Commands ==="));
    }

    /**
     * Registers the command as the server does for plugin.yml, under the plugin's name as its
     * namespace, and sends it from a sender holding every permission. The sender is not a player,
     * so nothing is reported, and a command that only prints needs no other collaborator.
     */
    private void dispatch(String commandLine, List<String> messages) {
        StubServer.install();
        NetherAccessController plugin = ListenerFixtures.pluginWith(new YamlConfiguration());
        SimpleCommandMap commandMap = new SimpleCommandMap(null);
        commandMap.register("NetherAccessController", new Command("nac") {
            @Override
            public boolean execute(CommandSender sender, String label, String[] args) {
                return plugin.onCommand(sender, this, label, args);
            }
        });
        CommandSender sender = ListenerFixtures.proxy(CommandSender.class, (proxy, method, args) -> {
            if (method.getName().equals("sendMessage") && args[0] instanceof String) {
                messages.add((String) args[0]);
                return null;
            }
            if (method.getName().equals("hasPermission")) {
                return true;
            }
            return ListenerFixtures.defaultValueFor(method.getReturnType());
        });

        assertTrue(commandMap.dispatch(sender, commandLine));
    }
}
