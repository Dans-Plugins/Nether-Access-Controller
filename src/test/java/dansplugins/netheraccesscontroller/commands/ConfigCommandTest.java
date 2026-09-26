package dansplugins.netheraccesscontroller.commands;

import dansplugins.netheraccesscontroller.services.ConfigService;
import dansplugins.netheraccesscontroller.utils.ArgumentParser;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers how {@link ConfigCommand#execute} turns the arguments of /nac config into a call on the
 * config service: which sub-command runs, which argument becomes the value, and which malformed
 * inputs stop before the service is reached. The value parsing and storage that follow are covered
 * by ConfigServiceTest.
 *
 * The config service is replaced by a subclass that records the call instead of writing to a
 * configuration, and {@link CommandSender} is stubbed with a {@link Proxy}, matching the other tests
 * in this project: no mocking dependency is declared, and only sendMessage is needed.
 */
class ConfigCommandTest {

    private final RecordingConfigService configService = new RecordingConfigService();
    private final RecordingSender sender = new RecordingSender();
    private final ConfigCommand configCommand = new ConfigCommand(configService, new ArgumentParser());

    @Test
    void noArguments_listsTheSubCommands() {
        boolean result = execute();

        assertFalse(result);
        assertEquals(1, sender.messages.size());
        assertTrue(sender.messages.get(0).contains("Sub-commands: show, set"));
        assertFalse(configService.listSent);
        assertNull(configService.option);
    }

    @Test
    void unknownSubCommand_listsTheSubCommands() {
        boolean result = execute("reset");

        assertFalse(result);
        assertEquals(1, sender.messages.size());
        assertTrue(sender.messages.get(0).contains("Sub-commands: show, set"));
        assertNull(configService.option);
    }

    @Test
    void show_sendsTheConfigList() {
        boolean result = execute("show");

        assertTrue(result);
        assertTrue(configService.listSent);
        assertNull(configService.option);
    }

    @Test
    void subCommand_isMatchedCaseInsensitively() {
        assertTrue(execute("SHOW"));
        assertTrue(configService.listSent);
    }

    @Test
    void set_withTooFewArguments_printsUsageWithoutSetting() {
        boolean result = execute("set", "preventPortalUsage");

        assertFalse(result);
        assertEquals(1, sender.messages.size());
        assertTrue(sender.messages.get(0).contains("Usage: /nac config set (option) (value)"));
        assertNull(configService.option);
    }

    @Test
    void set_passesTheOptionAndValueThrough() {
        boolean result = execute("set", "preventPortalUsage", "false");

        assertTrue(result);
        assertEquals("preventPortalUsage", configService.option);
        assertEquals("false", configService.value);
    }

    /**
     * Characterizes current behavior: only the two message options read quoted text, so any other
     * option takes the third argument as it is, quotes included, and ignores the rest.
     */
    @Test
    void set_onANonMessageOption_takesOnlyTheThirdArgument() {
        boolean result = execute("set", "debugMode", "'true", "please'");

        assertTrue(result);
        assertEquals("debugMode", configService.option);
        assertEquals("'true", configService.value);
    }

    @Test
    void set_denyUsageMessage_takesTheQuotedText() {
        boolean result = execute("set", "denyUsageMessage", "'You", "cannot", "enter", "the", "nether.'");

        assertTrue(result);
        assertEquals("denyUsageMessage", configService.option);
        assertEquals("You cannot enter the nether.", configService.value);
    }

    @Test
    void set_denyCreationMessage_takesTheQuotedText() {
        boolean result = execute("set", "denyCreationMessage", "'No", "portals", "here.'");

        assertTrue(result);
        assertEquals("denyCreationMessage", configService.option);
        assertEquals("No portals here.", configService.value);
    }

    @Test
    void set_messageOption_takesOnlyTheFirstQuotedSection() {
        execute("set", "denyUsageMessage", "'first'", "'second'");

        assertEquals("first", configService.value);
    }

    /**
     * The message options are recognised here regardless of case, but the option name is passed on
     * as typed, and ConfigService matches it exactly; see ConfigServiceTest.
     */
    @Test
    void set_messageOption_isRecognisedCaseInsensitively() {
        execute("set", "DENYUSAGEMESSAGE", "'Stay", "out.'");

        assertEquals("DENYUSAGEMESSAGE", configService.option);
        assertEquals("Stay out.", configService.value);
    }

    @Test
    void set_messageOptionWithoutQuotes_isRefusedWithoutSetting() {
        boolean result = execute("set", "denyUsageMessage", "Stay", "out.");

        assertFalse(result);
        assertEquals(1, sender.messages.size());
        assertTrue(sender.messages.get(0).contains("New message must be in between single quotes."));
        assertNull(configService.option);
    }

    @Test
    void set_messageOptionWithAnUnclosedQuote_isRefusedWithoutSetting() {
        boolean result = execute("set", "denyUsageMessage", "'Stay", "out.");

        assertFalse(result);
        assertTrue(sender.messages.get(0).contains("New message must be in between single quotes."));
        assertNull(configService.option);
    }

    private boolean execute(String... args) {
        CommandSender senderProxy = (CommandSender) Proxy.newProxyInstance(
                CommandSender.class.getClassLoader(), new Class<?>[]{CommandSender.class}, sender);
        return configCommand.execute(senderProxy, args);
    }

    /**
     * Records the option and value it is asked to set, and whether the config list was requested.
     */
    private static class RecordingConfigService extends ConfigService {
        private String option;
        private String value;
        private boolean listSent;

        RecordingConfigService() {
            super(null);
        }

        @Override
        public void setConfigOption(String option, String value, CommandSender sender) {
            this.option = option;
            this.value = value;
        }

        @Override
        public void sendConfigList(CommandSender sender) {
            listSent = true;
        }
    }

    /**
     * Records every message sent to it.
     */
    private static class RecordingSender implements InvocationHandler {
        private final List<String> messages = new ArrayList<>();

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            if (method.getName().equals("sendMessage")
                    && args != null && args.length > 0 && args[0] instanceof String) {
                messages.add((String) args[0]);
                return null;
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
