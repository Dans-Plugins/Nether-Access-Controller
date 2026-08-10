package dansplugins.netheraccesscontroller.services;

import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the permission gate in {@link CommandService#interpretCommand}, which is the
 * single point where every sub-command's permission node is enforced. A sub-command that
 * runs without its node lets a restricted player reconfigure or inspect the plugin that is
 * meant to restrict them, so each branch is asserted to stop before its command object.
 *
 * {@link CommandSender} is stubbed with a {@link Proxy} rather than a mocking library: the
 * project declares no mocking dependency, and only hasPermission and sendMessage are needed.
 * Sub-commands that require a live Bukkit server to run to completion are out of scope here
 * and are covered by manual validation instead.
 */
class CommandServiceTest {

    /**
     * The collaborators are null because no test in this class reaches a code path that
     * uses one: every denied sub-command returns before its command object is constructed,
     * and the one permitted case exercises ConfigCommand's empty-argument branch, which
     * returns before touching the config service.
     */
    private final CommandService commandService = new CommandService(null, null, null, null, null);

    @Test
    void config_withoutPermission_doesNotExecuteTheCommand() {
        RecordingSender sender = new RecordingSender();

        boolean result = commandService.interpretCommand(senderFor(sender), "nac", new String[]{"config"});

        assertFalse(result);
        // Exactly one message proves ConfigCommand was never reached: had it run, it would
        // have appended its own "Sub-commands: show, set" line after the denial.
        assertEquals(1, sender.messages.size());
        assertTrue(sender.messages.get(0).contains("nac.config"));
    }

    @Test
    void configSet_withoutPermission_doesNotReachTheConfigService() {
        RecordingSender sender = new RecordingSender();

        // The damaging case: without the gate this reaches ConfigService.setConfigOption and
        // disables whitelist enforcement for everyone. A null config service would surface
        // that as a NullPointerException, so reaching it fails this test either way.
        boolean result = commandService.interpretCommand(
                senderFor(sender), "nac", new String[]{"config", "set", "preventPortalCreation", "false"});

        assertFalse(result);
        assertEquals(1, sender.messages.size());
        assertTrue(sender.messages.get(0).contains("nac.config"));
    }

    @Test
    void config_withPermission_reachesTheCommand() {
        RecordingSender sender = new RecordingSender("nac.config");

        boolean result = commandService.interpretCommand(senderFor(sender), "nac", new String[]{"config"});

        assertFalse(result);
        assertEquals(1, sender.messages.size());
        assertTrue(sender.messages.get(0).contains("Sub-commands: show, set"));
    }

    @Test
    void help_withoutPermission_doesNotExecuteTheCommand() {
        assertDeniedBeforeExecution("help", "nac.help");
    }

    @Test
    void list_withoutPermission_doesNotExecuteTheCommand() {
        assertDeniedBeforeExecution("list", "nac.list");
    }

    @Test
    void allow_withoutPermission_doesNotExecuteTheCommand() {
        assertDeniedBeforeExecution("allow", "nac.allow");
    }

    @Test
    void deny_withoutPermission_doesNotExecuteTheCommand() {
        assertDeniedBeforeExecution("deny", "nac.deny");
    }

    /**
     * Characterizes the guard shared by every sub-command: a sender lacking the node is told
     * which node is missing and nothing else happens.
     */
    private void assertDeniedBeforeExecution(String subCommand, String permission) {
        RecordingSender sender = new RecordingSender();

        boolean result = commandService.interpretCommand(senderFor(sender), "nac", new String[]{subCommand, "Steve"});

        assertFalse(result);
        assertEquals(1, sender.messages.size());
        assertTrue(sender.messages.get(0).contains(permission));
    }

    private CommandSender senderFor(RecordingSender handler) {
        return (CommandSender) Proxy.newProxyInstance(
                CommandSender.class.getClassLoader(), new Class<?>[]{CommandSender.class}, handler);
    }

    /**
     * Grants only the permissions it is given and records every message sent to it.
     */
    private static class RecordingSender implements InvocationHandler {
        private final Set<String> grantedPermissions;
        private final List<String> messages = new ArrayList<>();

        RecordingSender(String... grantedPermissions) {
            this.grantedPermissions = new HashSet<>(Arrays.asList(grantedPermissions));
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            if (args != null && args.length > 0 && args[0] instanceof String) {
                if (method.getName().equals("hasPermission")) {
                    return grantedPermissions.contains(args[0]);
                }
                if (method.getName().equals("sendMessage")) {
                    messages.add((String) args[0]);
                    return null;
                }
            }
            return defaultValueFor(method.getReturnType());
        }

        private Object defaultValueFor(Class<?> returnType) {
            if (returnType.equals(boolean.class)) {
                return false;
            }
            if (returnType.equals(int.class)) {
                return 0;
            }
            return null;
        }
    }
}
