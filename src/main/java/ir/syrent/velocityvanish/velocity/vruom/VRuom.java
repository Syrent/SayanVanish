package ir.syrent.velocityvanish.velocity.vruom;

import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class VRuom {

    private final static Plugin DESCRIPTION;
    private final static Logger logger;
    private static boolean debug = false;

    static {
        DESCRIPTION = VRUoMPlugin.get().getClass().getAnnotation(Plugin.class);
        logger = VRUoMPlugin.getLogger();
    }

    public static Object getPlugin() {
        return VRUoMPlugin.get();
    }

    public static Plugin getDescription() {
        return DESCRIPTION;
    }

    public static ProxyServer getServer() {
        return VRUoMPlugin.getServer();
    }

    public static Path getDataDirectory() {
        return VRUoMPlugin.getDataDirectory();
    }

    /**
     * @deprecated Use {@link #getDataDirectory()} instead
     * Data folder is accessible through the DataDirectory constructor in the Velocity main class\
     */
    public static File getDataFolder() {
        return new File("plugins", getDescription().name());
    }

    public static void registerListener(Object listener) {
        getServer().getEventManager().register(VRUoMPlugin.get(), listener);
    }

    public static void registerCommand(String name, Collection<String> aliases, Command command) {
        CommandMeta meta = getServer().getCommandManager().metaBuilder(name).aliases(aliases.toArray(new String[0])).build();
        getServer().getCommandManager().register(meta, command);
    }

    public static Collection<Player> getOnlinePlayers() {
        return getServer().getAllPlayers();
    }

    public static Optional<Player> getPlayer(String username) {
        for (Player player : getOnlinePlayers()) {
            if (player.getUsername().equalsIgnoreCase(username)) {
                return Optional.of(player);
            }
        }
        return Optional.empty();
    }

    public static void setDebug(boolean debug) {
        VRuom.debug = debug;
    }

    public static void log(String message) {
        logger.info(message);
    }

    public static void debug(String message) {
        if (debug) {
            log("[Debug] " + message);
        }
    }

    public static void warn(String message) {
        logger.warn(message);
    }

    public static void error(String message) {
        logger.error(message);
    }

    public static void run(RunnableExc runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FunctionalInterface public interface RunnableExc {
        void run() throws Exception;
    }

    public static ScheduledTask runAsync(Runnable runnable) {
        return getServer().getScheduler().buildTask(VRUoMPlugin.get(), runnable).schedule();
    }

    public static ScheduledTask runAsync(Runnable runnable, long delay, TimeUnit delayUnit) {
        return getServer().getScheduler().buildTask(VRUoMPlugin.get(), runnable).delay(delay, delayUnit).schedule();
    }

    public static ScheduledTask runAsync(Runnable runnable, long delay, TimeUnit delayUnit, long period, TimeUnit periodUnit) {
        return getServer().getScheduler().buildTask(VRUoMPlugin.get(), runnable).delay(delay, delayUnit).repeat(period, periodUnit).schedule();
    }

}