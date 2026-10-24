package dansplugins.netheraccesscontroller.utils;

import dansplugins.netheraccesscontroller.NetherAccessController;
import dansplugins.netheraccesscontroller.data.PersistentData;
import dansplugins.netheraccesscontroller.listeners.InteractionListener;
import dansplugins.netheraccesscontroller.listeners.PlayerPortalEventListener;
import dansplugins.netheraccesscontroller.services.ConfigService;
import org.bukkit.Server;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the registration of the plugin's two listeners, which are its access checks. A listener
 * that is not registered never runs, and nothing reports its absence: every player can then use or
 * create nether portals regardless of the whitelist and the prevent options.
 *
 * The plugin is allocated without running any constructor, for the reason given in the listener
 * tests' ListenerFixtures, and the server it would have been given is placed in JavaPlugin's
 * private server field, so that getServer() returns a stub whose plugin manager records what is
 * registered with it. Like the other tests in this project, the server and plugin manager are
 * stubbed with a {@link Proxy} because no mocking dependency is declared.
 */
class EventRegistryTest {

    private final List<Listener> registeredListeners = new ArrayList<>();
    private final List<Plugin> registeringPlugins = new ArrayList<>();
    private NetherAccessController plugin;
    private ConfigService configService;
    private PersistentData persistentData;

    @BeforeEach
    void setUp() {
        PluginManager pluginManager = proxy(PluginManager.class, (proxy, method, args) -> {
            if (method.getName().equals("registerEvents")) {
                registeredListeners.add((Listener) args[0]);
                registeringPlugins.add((Plugin) args[1]);
            }
            return null;
        });
        Server server = proxy(Server.class, (proxy, method, args) ->
                method.getName().equals("getPluginManager") ? pluginManager : null);

        plugin = pluginWith(server);
        configService = new ConfigService(plugin);
        persistentData = new PersistentData();
    }

    @Test
    void registerEvents_registersExactlyThePortalUsageAndPortalCreationListeners() {
        new EventRegistry(plugin, configService, persistentData).registerEvents();

        assertEquals(2, registeredListeners.size());
        assertEquals(1, countOf(PlayerPortalEventListener.class));
        assertEquals(1, countOf(InteractionListener.class));
    }

    @Test
    void registerEvents_registersEachListenerOnBehalfOfThePlugin() {
        new EventRegistry(plugin, configService, persistentData).registerEvents();

        for (Plugin registeringPlugin : registeringPlugins) {
            assertSame(plugin, registeringPlugin);
        }
    }

    /**
     * Bukkit routes an event to a registered listener only through a method annotated with
     * {@link EventHandler} whose parameter is that event, so registration alone does not show that
     * the checks run; this pins which event each registered listener answers.
     */
    @Test
    void registerEvents_registeredListenersHandleThePortalAndInteractEvents() {
        new EventRegistry(plugin, configService, persistentData).registerEvents();

        List<Class<?>> handledEvents = new ArrayList<>();
        for (Listener listener : registeredListeners) {
            for (Method method : listener.getClass().getMethods()) {
                if (method.isAnnotationPresent(EventHandler.class) && method.getParameterCount() == 1) {
                    handledEvents.add(method.getParameterTypes()[0]);
                }
            }
        }

        assertTrue(handledEvents.contains(PlayerPortalEvent.class));
        assertTrue(handledEvents.contains(PlayerInteractEvent.class));
    }

    private long countOf(Class<? extends Listener> listenerClass) {
        return registeredListeners.stream().filter(listenerClass::isInstance).count();
    }

    private static NetherAccessController pluginWith(Server server) {
        try {
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            Field theUnsafe = unsafeClass.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            Object unsafe = theUnsafe.get(null);
            Method allocateInstance = unsafeClass.getMethod("allocateInstance", Class.class);
            NetherAccessController plugin = (NetherAccessController) allocateInstance.invoke(unsafe, NetherAccessController.class);

            Field serverField = JavaPlugin.class.getDeclaredField("server");
            serverField.setAccessible(true);
            serverField.set(plugin, server);
            return plugin;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not stand up the plugin for an event registry test", e);
        }
    }

    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler));
    }
}
