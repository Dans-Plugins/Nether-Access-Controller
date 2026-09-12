package dansplugins.netheraccesscontroller.services;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the value parsing in {@link ConfigService#setConfigOption}, which is the only path by
 * which an operator changes an enforcement option in game. The damaging case is a boolean: a value
 * that is not recognised must leave the stored value alone, because the alternative — storing it as
 * false — turns preventPortalCreation off and lets every player create nether portals, whitelist
 * or not. Each unrecognised value is therefore asserted to be both refused and not written.
 *
 * The plugin itself cannot be constructed outside a live Bukkit server, so an in-memory
 * {@link YamlConfiguration} is supplied in its place and the save is suppressed. {@link
 * CommandSender} is stubbed with a {@link Proxy} rather than a mocking library, matching
 * {@link CommandServiceTest}: the project declares no mocking dependency, and only sendMessage is
 * needed here.
 */
class ConfigServiceTest {

    private final FileConfiguration config = defaultConfig();
    private final RecordingSender sender = new RecordingSender();
    private final ConfigService configService = new InMemoryConfigService(config);

    @Test
    void setConfigOption_withTrue_storesTrue() {
        configService.setConfigOption("preventPortalUsage", "true", senderProxy());

        assertTrue(config.getBoolean("preventPortalUsage"));
        assertTrue(lastMessage().contains("Boolean set."));
    }

    @Test
    void setConfigOption_withFalse_storesFalse() {
        configService.setConfigOption("preventPortalCreation", "false", senderProxy());

        assertFalse(config.getBoolean("preventPortalCreation"));
        assertTrue(lastMessage().contains("Boolean set."));
    }

    /**
     * Only the value is case-insensitive. The option name is not — see
     * {@link #setConfigOption_withDifferentlyCasedOptionName_isRejectedAsUnknown}.
     */
    @Test
    void setConfigOption_withDifferentlyCasedBoolean_isAccepted() {
        configService.setConfigOption("preventPortalUsage", "TRUE", senderProxy());

        assertTrue(config.getBoolean("preventPortalUsage"));
        assertTrue(lastMessage().contains("Boolean set."));
    }

    /**
     * The regression this class exists for: 'yes' was previously parsed as false, reported as
     * "Boolean set." in green, and written — disabling portal-creation enforcement in response to
     * a value the operator plainly meant as an enabling one.
     */
    @Test
    void setConfigOption_withYes_isRefusedAndLeavesEnforcementOn() {
        configService.setConfigOption("preventPortalCreation", "yes", senderProxy());

        assertTrue(config.getBoolean("preventPortalCreation"),
                "An unrecognised value must not disable portal-creation enforcement");
        assertRefused("yes", "preventPortalCreation");
    }

    @Test
    void setConfigOption_withMisspeltTrue_isRefusedAndLeavesEnforcementOn() {
        configService.setConfigOption("preventPortalCreation", "ture", senderProxy());

        assertTrue(config.getBoolean("preventPortalCreation"));
        assertRefused("ture", "preventPortalCreation");
    }

    @Test
    void setConfigOption_withOne_isRefusedAndLeavesEnforcementOn() {
        configService.setConfigOption("preventPortalCreation", "1", senderProxy());

        assertTrue(config.getBoolean("preventPortalCreation"));
        assertRefused("1", "preventPortalCreation");
    }

    /**
     * A refused value must also leave an option that was previously turned on turned on, rather
     * than only preserving the shipped default.
     */
    @Test
    void setConfigOption_withUnrecognisedValue_leavesAPreviouslySetValueIntact() {
        config.set("preventPortalUsage", true);

        configService.setConfigOption("preventPortalUsage", "enabled", senderProxy());

        assertTrue(config.getBoolean("preventPortalUsage"));
        assertRefused("enabled", "preventPortalUsage");
    }

    @Test
    void setConfigOption_withStringOption_storesTheValueVerbatim() {
        configService.setConfigOption("denyUsageMessage", "Keep out.", senderProxy());

        assertEquals("Keep out.", config.getString("denyUsageMessage"));
        assertTrue(lastMessage().contains("String set."));
    }

    @Test
    void setConfigOption_withVersion_isRefusedAndLeavesTheVersionIntact() {
        configService.setConfigOption("version", "v9.9.9", senderProxy());

        assertEquals("v2.0.0", config.getString("version"));
        assertTrue(lastMessage().contains("Cannot set version."));
        assertFalse(configService.hasBeenAltered());
    }

    @Test
    void setConfigOption_withUnknownOption_isRefused() {
        configService.setConfigOption("nonexistentOption", "true", senderProxy());

        assertFalse(config.isSet("nonexistentOption"));
        assertTrue(lastMessage().contains("wasn't found."));
    }

    /**
     * Option names are matched exactly. The isSet gate is case-sensitive, so a differently-cased
     * spelling is reported as unknown rather than being treated as the boolean option it resembles.
     */
    @Test
    void setConfigOption_withDifferentlyCasedOptionName_isRejectedAsUnknown() {
        configService.setConfigOption("PREVENTPORTALCREATION", "false", senderProxy());

        assertTrue(config.getBoolean("preventPortalCreation"));
        assertTrue(lastMessage().contains("wasn't found."));
    }

    /**
     * hasBeenAltered is what tells StorageService the config is worth saving, so a refused value
     * must not set it.
     */
    @Test
    void setConfigOption_withUnrecognisedValue_doesNotMarkTheConfigAltered() {
        configService.setConfigOption("debugMode", "on", senderProxy());

        assertFalse(configService.hasBeenAltered());
    }

    @Test
    void setConfigOption_withRecognisedValue_marksTheConfigAltered() {
        configService.setConfigOption("debugMode", "true", senderProxy());

        assertTrue(configService.hasBeenAltered());
    }

    /**
     * debugMode is rendered from the same boolean accessor as its two siblings, so a value stored
     * as a boolean is listed as one rather than relying on the string form happening to match.
     */
    @Test
    void sendConfigList_rendersEveryBooleanOptionAsABoolean() {
        configService.sendConfigList(senderProxy());

        assertTrue(lastMessage().contains("debugMode: false"));
        assertTrue(lastMessage().contains("preventPortalUsage: false"));
        assertTrue(lastMessage().contains("preventPortalCreation: true"));
    }

    /**
     * A server upgraded from before usage reporting has no usage-reporting block in its
     * config.yml, and saveMissingConfigDefaultsIfNotPresent only runs on a version change. Bukkit
     * registers the jar's config.yml as the defaults for the file, and the one-argument getters
     * fall through to them; the two-argument ones return their fallback instead, which for the key
     * would be "" and would turn reporting off on every existing installation. The defaults here
     * are the bundled file itself, so this also pins that the jar carries a key.
     */
    @Test
    void usageReporting_readsThroughToTheBundledDefaultsWhenTheFileHasNoBlock() {
        config.setDefaults(bundledConfig());

        assertTrue(configService.isUsageReportingEnabled());
        assertEquals("https://trace.danielstephenson.dev", configService.getUsageReportingEndpoint());
        assertFalse(configService.getUsageReportingKey().isEmpty(),
                "The bundled config.yml must carry the plugin's key");
        assertEquals(config.getDefaults().getString("usage-reporting.key"), configService.getUsageReportingKey());

        // The trap being guarded against, measured rather than assumed: the two-argument getters
        // do not consult the defaults, so reading through them would report no key at all.
        assertEquals("", config.getString("usage-reporting.key", ""));
        assertFalse(config.getBoolean("usage-reporting.enabled", false));
    }

    @Test
    void usageReporting_isOffWithNoKeyAnywhere() {
        assertEquals("", configService.getUsageReportingKey(), "no key anywhere must read as off, not as null");
        assertEquals("https://trace.danielstephenson.dev", configService.getUsageReportingEndpoint());
        assertFalse(configService.isUsageReportingEnabled());
    }

    @Test
    void usageReporting_readsTheConfiguredValuesOverTheBundledDefaults() {
        config.setDefaults(bundledConfig());
        config.set("usage-reporting.enabled", false);
        config.set("usage-reporting.endpoint", "http://localhost:8080");
        config.set("usage-reporting.key", "abc");

        assertFalse(configService.isUsageReportingEnabled());
        assertEquals("http://localhost:8080", configService.getUsageReportingEndpoint());
        assertEquals("abc", configService.getUsageReportingKey());
    }

    /**
     * The switch is a boolean option like the three beside it: an in-game 'false' is stored as a
     * boolean, and an unrecognised value is refused rather than stored as a string that
     * getBoolean would then read as false.
     */
    @Test
    void setConfigOption_withUsageReportingEnabled_storesABoolean() {
        config.set("usage-reporting.enabled", true);

        configService.setConfigOption("usage-reporting.enabled", "false", senderProxy());

        assertFalse(configService.isUsageReportingEnabled());
        assertTrue(lastMessage().contains("Boolean set."));
    }

    @Test
    void setConfigOption_withUsageReportingEnabled_refusesAnUnrecognisedValue() {
        config.set("usage-reporting.enabled", true);

        configService.setConfigOption("usage-reporting.enabled", "off", senderProxy());

        assertTrue(configService.isUsageReportingEnabled());
        assertRefused("off", "usage-reporting.enabled");
    }

    @Test
    void sendConfigList_namesTheUsageReportingSwitch() {
        config.setDefaults(bundledConfig());

        configService.sendConfigList(senderProxy());

        assertTrue(lastMessage().contains("usage-reporting.enabled: true"));
    }

    /**
     * The config.yml shipped in the jar, which Bukkit registers as the defaults for the file on
     * disk. Read from the test classpath so that the test sees the file as built.
     */
    private static YamlConfiguration bundledConfig() {
        InputStream bundled = ConfigServiceTest.class.getResourceAsStream("/config.yml");
        assertNotNull(bundled, "src/main/resources/config.yml must be bundled in the jar");
        return YamlConfiguration.loadConfiguration(new InputStreamReader(bundled, StandardCharsets.UTF_8));
    }

    private void assertRefused(String value, String option) {
        assertTrue(lastMessage().contains(value),
                "The refusal should name the value that was rejected");
        assertTrue(lastMessage().contains(option),
                "The refusal should name the option it applies to");
        assertTrue(lastMessage().contains("true") && lastMessage().contains("false"),
                "The refusal should name the values that are accepted");
    }

    private String lastMessage() {
        assertFalse(sender.messages.isEmpty(), "Expected a message to have been sent");
        return sender.messages.get(sender.messages.size() - 1);
    }

    private CommandSender senderProxy() {
        return (CommandSender) Proxy.newProxyInstance(
                CommandSender.class.getClassLoader(), new Class<?>[]{CommandSender.class}, sender);
    }

    /**
     * Mirrors what saveMissingConfigDefaultsIfNotPresent writes on first run.
     */
    private static FileConfiguration defaultConfig() {
        FileConfiguration config = new YamlConfiguration();
        config.set("version", "v2.0.0");
        config.set("debugMode", false);
        config.set("denyUsageMessage", "You're unable to use nether portals.");
        config.set("denyCreationMessage", "You're unable to create nether portals.");
        config.set("preventPortalUsage", false);
        config.set("preventPortalCreation", true);
        return config;
    }

    /**
     * Reads and writes an in-memory configuration instead of the plugin's own, which cannot be
     * obtained without a running server.
     */
    private static class InMemoryConfigService extends ConfigService {
        private final FileConfiguration config;

        InMemoryConfigService(FileConfiguration config) {
            super(null);
            this.config = config;
        }

        @Override
        public FileConfiguration getConfig() {
            return config;
        }

        @Override
        void saveConfig() {
            // There is no file to write to; the assertions read the configuration directly.
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
