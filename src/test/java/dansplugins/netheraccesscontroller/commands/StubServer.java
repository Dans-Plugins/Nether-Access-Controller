package dansplugins.netheraccesscontroller.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;

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
 * Test support for the commands that look a player up through {@link Bukkit#getOfflinePlayer(UUID)}.
 *
 * Bukkit holds its server in a static field that can be set only once per JVM, so the stub is
 * installed on first use and shared by every test class that needs it. Players are registered by
 * UUID with the name the server knows them by; getOfflinePlayer answers with that name and every
 * other server method returns its type's default. Like the other tests in this project, the server,
 * player, and sender are stubbed with a {@link Proxy} because no mocking dependency is declared.
 */
final class StubServer {

    private static final Map<UUID, String> PLAYER_NAMES = new HashMap<>();

    private StubServer() {
    }

    static synchronized void registerPlayer(UUID uuid, String name) {
        if (Bukkit.getServer() == null) {
            Bukkit.setServer(proxy(Server.class, StubServer::answerServer));
        }
        PLAYER_NAMES.put(uuid, name);
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
        if (method.getName().equals("getLogger")) {
            // Bukkit.setServer logs the server's name and version through this logger.
            return Logger.getLogger(StubServer.class.getName());
        }
        if (method.getReturnType().equals(String.class)) {
            return "stub";
        }
        return defaultValueFor(method.getReturnType());
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
