package ir.sayandevelopment.spigot;

import ir.sayandevelopment.VanishManager;
import ir.sayandevelopment.database.MySQL;
import ir.sayandevelopment.spigot.listener.AsyncPlayerChatListener;
import ir.sayandevelopment.spigot.listener.PlayerMentionListener;
import ir.sayandevelopment.listener.PrivateMessageListener;
import ir.sayandevelopment.spigot.bridge.BukkitBridgeListener;
import ir.sayandevelopment.spigot.dependency.PlaceholderAPI;
import ir.sayandevelopment.spigot.listener.PlayerJoinListener;
import ir.sayandevelopment.spigot.listener.TabCompleteListener;
import me.mohamad82.ruom.RUoMPlugin;
import me.mohamad82.ruom.Ruom;
import me.mohamad82.ruom.configuration.YamlConfig;
import org.bukkit.Bukkit;

import java.util.HashSet;
import java.util.Set;

public class SpigotMain extends RUoMPlugin {

    private static SpigotMain instance;
    public static SpigotMain getInstance() {
        return instance;
    }
    public static Set<String> vanishedPlayers = new HashSet<>();

    public static MySQL SQL;
    public YamlConfig configYML;

    @Override
    public void onEnable() {
        instance = this;
        SQL = new MySQL();

        try {
            this.getLogger().info("Connecting to SQL...");
            SQL.connect();
            SQL.init();
            this.getLogger().info("Connected to SQL.");
        } catch (Exception ex) {
            this.getLogger().severe("Error while connecting to SQL.");
            ex.printStackTrace();
            return;
        }

        Ruom.runAsync(() -> {
            vanishedPlayers.clear();
            try {
                if (SQL.getVanishedPlayers() != null) {
                    vanishedPlayers.addAll(SQL.getVanishedPlayers());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 5);

        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "sayanvanish:main");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "sayanvanish:main", new BukkitBridgeListener());

        new VanishManager();

        Ruom.registerListener(new PlayerJoinListener());
        Ruom.registerListener(new AsyncPlayerChatListener());
        //Ruom.registerListener(new PrivateMessageListener());
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
        Ruom.shutdown();
    }
}
