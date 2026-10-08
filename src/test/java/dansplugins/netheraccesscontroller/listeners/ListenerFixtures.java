package dansplugins.netheraccesscontroller.listeners;

import dansplugins.netheraccesscontroller.NetherAccessController;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Test support for the two listeners, which are the plugin's access checks: a player either gets
 * through a portal or does not on the strength of what they decide.
 *
 * The listeners take the plugin itself, which cannot be constructed outside a live server —
 * JavaPlugin's constructor refuses any class loader but the server's own, and the plugin's field
 * initialisers read a plugin description that only the server supplies. The plugin is therefore
 * allocated without running any constructor, and the configuration it would have loaded from disk
 * is placed in JavaPlugin's private newConfig field, so that getConfig() returns it instead of
 * trying to read a file. ConfigService reads through the same getConfig(), so one in-memory
 * configuration drives both the plugin's debug switch and the listeners' enforcement options.
 *
 * Players, blocks and inventories are stubbed with a {@link Proxy}, as elsewhere in this project,
 * because no mocking dependency is declared.
 */
final class ListenerFixtures {

    private ListenerFixtures() {
    }

    /**
     * Mirrors what saveMissingConfigDefaultsIfNotPresent writes on first run.
     */
    static FileConfiguration defaultConfig() {
        FileConfiguration config = new YamlConfiguration();
        config.set("version", "v2.0.0");
        config.set("debugMode", false);
        config.set("denyUsageMessage", "You're unable to use nether portals.");
        config.set("denyCreationMessage", "You're unable to create nether portals.");
        config.set("preventPortalUsage", false);
        config.set("preventPortalCreation", true);
        return config;
    }

    static NetherAccessController pluginWith(FileConfiguration config) {
        try {
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            Field theUnsafe = unsafeClass.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            Object unsafe = theUnsafe.get(null);
            Method allocateInstance = unsafeClass.getMethod("allocateInstance", Class.class);
            NetherAccessController plugin = (NetherAccessController) allocateInstance.invoke(unsafe, NetherAccessController.class);

            Field newConfig = JavaPlugin.class.getDeclaredField("newConfig");
            newConfig.setAccessible(true);
            newConfig.set(plugin, config);
            return plugin;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not stand up the plugin for a listener test", e);
        }
    }

    static Block block(Material material) {
        BlockData blockData = proxy(BlockData.class, (proxy, method, args) ->
                method.getName().equals("getMaterial") ? material : defaultValueFor(method.getReturnType()));
        return proxy(Block.class, (proxy, method, args) ->
                method.getName().equals("getBlockData") ? blockData : defaultValueFor(method.getReturnType()));
    }

    static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler));
    }

    static Object defaultValueFor(Class<?> returnType) {
        if (returnType.equals(boolean.class)) {
            return false;
        }
        if (returnType.equals(int.class)) {
            return 0;
        }
        if (returnType.equals(long.class)) {
            return 0L;
        }
        if (returnType.equals(double.class)) {
            return 0.0;
        }
        if (returnType.equals(float.class)) {
            return 0.0f;
        }
        return null;
    }

    /**
     * A player with a fixed UUID who holds the given items and records every message sent to them.
     */
    static class RecordingPlayer implements InvocationHandler {
        final UUID uuid = UUID.randomUUID();
        final List<String> messages = new ArrayList<>();
        ItemStack mainHand = new ItemStack(Material.AIR);
        ItemStack offHand = new ItemStack(Material.AIR);

        Player player() {
            return proxy(Player.class, this);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            switch (method.getName()) {
                case "getUniqueId":
                    return uuid;
                case "getName":
                    return "Steve";
                case "getInventory":
                    return proxy(PlayerInventory.class, (inventory, inventoryMethod, inventoryArgs) -> {
                        if (inventoryMethod.getName().equals("getItemInMainHand")) {
                            return mainHand;
                        }
                        if (inventoryMethod.getName().equals("getItemInOffHand")) {
                            return offHand;
                        }
                        return defaultValueFor(inventoryMethod.getReturnType());
                    });
                case "sendMessage":
                    if (args != null && args.length == 1 && args[0] instanceof String) {
                        messages.add((String) args[0]);
                    }
                    return null;
                default:
                    return defaultValueFor(method.getReturnType());
            }
        }
    }
}
