package dansplugins.netheraccesscontroller.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Test support for the code that looks a player up through {@link Bukkit#getOfflinePlayer(UUID)},
 * {@link Bukkit#getOnlinePlayers()}, or {@link Bukkit#getOfflinePlayers()}.
 *
 * Bukkit holds its server in a static field that can be set only once per JVM, so the stub is
 * installed on first use and shared by every test class that needs it, in this package or another.
 * Players are registered by UUID with the name the server knows them by; getOfflinePlayer answers
 * with that name. The lists behind getOnlinePlayers and getOfflinePlayers are filled separately and
 * emptied by {@link #clearPlayerLists()}. Every other server method returns its type's default.
 * Like the other tests in this project, the server, players, and sender are stubbed with a
 * {@link Proxy} because no mocking dependency is declared.
 */
public final class StubServer {

    private static final Map<UUID, String> PLAYER_NAMES = new HashMap<>();
    private static final List<Player> ONLINE_PLAYERS = new ArrayList<>();
    private static final List<OfflinePlayer> OFFLINE_PLAYERS = new ArrayList<>();

    private StubServer() {
    }

    static synchronized void registerPlayer(UUID uuid, String name) {
        install();
        PLAYER_NAMES.put(uuid, name);
    }

    /**
     * Adds a player to the list returned by getOnlinePlayers.
     */
    public static synchronized void addOnlinePlayer(UUID uuid, String name) {
        install();
        ONLINE_PLAYERS.add(proxy(Player.class, namedPlayer(uuid, name)));
    }

    /**
     * Adds a player to the array returned by getOfflinePlayers. The name may be null, as it is for
     * a player the server has a UUID for but no recorded name.
     */
    public static synchronized void addOfflinePlayer(UUID uuid, String name) {
        install();
        OFFLINE_PLAYERS.add(proxy(OfflinePlayer.class, namedPlayer(uuid, name)));
    }

    public static synchronized void clearPlayerLists() {
        ONLINE_PLAYERS.clear();
        OFFLINE_PLAYERS.clear();
    }

    private static void install() {
        if (Bukkit.getServer() == null) {
            Bukkit.setServer(proxy(Server.class, StubServer::answerServer));
        }
    }

    static CommandSender sender(RecordingSender recordingSender) {
        return proxy(CommandSender.class, recordingSender);
    }

    private static Object answerServer(Object proxy, Method method, Object[] args) {
        if (method.getName().equals("getOfflinePlayer")
                && args != null && args.length == 1 && args[0] instanceof UUID) {
            String name = PLAYER_NAMES.get(args[0]);
            return proxy(OfflinePlayer.class, (player, playerMethod, playerArgs) ->
                    playerMethod.getName().equals("getName") ? name : defaultValueFor(playerMethod.getReturnType()));
        }
        if (method.getName().equals("getOnlinePlayers")) {
            return new ArrayList<>(ONLINE_PLAYERS);
        }
        if (method.getName().equals("getOfflinePlayers")) {
            return OFFLINE_PLAYERS.toArray(new OfflinePlayer[0]);
        }
        if (method.getName().equals("getLogger")) {
            // Bukkit.setServer logs the server's name and version through this logger.
            return Logger.getLogger(StubServer.class.getName());
        }
        if (method.getReturnType().equals(String.class)) {
            return "stub";
        }
        return defaultValueFor(method.getReturnType());
    }

    private static InvocationHandler namedPlayer(UUID uuid, String name) {
        return (player, method, args) -> {
            if (method.getName().equals("getName")) {
                return name;
            }
            if (method.getName().equals("getUniqueId")) {
                return uuid;
            }
            return defaultValueFor(method.getReturnType());
        };
    }

    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler));
    }

    private static Object defaultValueFor(Class<?> returnType) {
        if (returnType.equals(boolean.class)) {
            return false;
        }
        if (returnType.equals(int.class)) {
            return 0;
        }
        if (returnType.equals(long.class)) {
            return 0L;
        }
        return null;
    }

    /**
     * Records every message sent to it.
     */
    static class RecordingSender implements InvocationHandler {
        final List<String> messages = new ArrayList<>();

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            if (method.getName().equals("sendMessage")
                    && args != null && args.length > 0 && args[0] instanceof String) {
                messages.add((String) args[0]);
                return null;
            }
            return defaultValueFor(method.getReturnType());
        }
    }
}
