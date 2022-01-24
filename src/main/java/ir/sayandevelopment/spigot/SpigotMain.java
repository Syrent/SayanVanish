package ir.sayandevelopment.spigot;

import ir.sayandevelopment.VanishManager;
import ir.sayandevelopment.VanishedPlayer;
import ir.sayandevelopment.database.MySQL;
import ir.sayandevelopment.database.SQL;
import ir.sayandevelopment.listener.AsyncPlayerChatListener;
import ir.sayandevelopment.listener.PlayerMentionListener;
import ir.sayandevelopment.listener.PrivateMessageListener;
import ir.sayandevelopment.spigot.bridge.BukkitBridgeListener;
import ir.sayandevelopment.spigot.dependency.PlaceholderAPI;
import ir.sayandevelopment.spigot.listener.PlayerJoinListener;
import ir.sayandevelopment.spigot.listener.PlayerQuitListener;
import ir.sayandevelopment.spigot.listener.TabCompleteListener;
import me.mohamad82.ruom.RUoMPlugin;
import me.mohamad82.ruom.Ruom;
import me.mohamad82.ruom.configuration.YamlConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.server.TabCompleteEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SpigotMain extends RUoMPlugin {

    private static SpigotMain instance;
    public static SpigotMain getInstance() {
        return instance;
    }
    public static Map<UUID, VanishedPlayer> vanishedPlayers = new HashMap<>();

    public static SQL SQL;
    public YamlConfig configYML;

    @Override
    public void onEnable() {
        instance = this;

        String host = "localhost";
        String database = "server";
        String user = "server";
        String pass = "yG%@NU6wz}i#)ZQN";
        int port = 3306;

        SQL = new MySQL(host, port, database, user, pass);

        try {
            this.getLogger().info("Connecting to SQL...");
            SQL.openConnection();
            SQL.createTable();
            this.getLogger().info("Connected to SQL.");
        } catch (Exception ex) {
            this.getLogger().severe("Error while connecting to SQL.");
            ex.printStackTrace();
            return;
        }

        Ruom.runAsync(() -> {
            Player syrent = Bukkit.getPlayerExact("Syrent231");
            vanishedPlayers.clear();
            try {
                if (SQL.getVanishedPlayers() != null) {
                    for (VanishedPlayer vanishedPlayer : SQL.getVanishedPlayers()) {
                        vanishedPlayers.put(vanishedPlayer.getUuid(), vanishedPlayer);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 1);

        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "sayanvanish:main");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "sayanvanish:main", new BukkitBridgeListener());

        new VanishManager();

        Ruom.registerListener(new PlayerJoinListener());
        Ruom.registerListener(new PlayerQuitListener());
        Ruom.registerListener(new AsyncPlayerChatListener());
        Ruom.registerListener(new PrivateMessageListener());
        Ruom.registerListener(new PlayerMentionListener());
        Ruom.registerListener(new TabCompleteListener());

        configYML = new YamlConfig(getDataFolder(), "config.yml", true);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderAPI().register();
        }
    }

    @Override
    public void onDisable() {
        //make sure to unregister the registered channels in case of a reload
        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        this.getServer().getMessenger().unregisterIncomingPluginChannel(this);
    }
}
