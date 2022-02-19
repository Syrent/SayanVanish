package ir.sayandevelopment;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import ir.sayandevelopment.command.FindCommand;
import ir.sayandevelopment.command.GListCommand;
import ir.sayandevelopment.command.VanishCommand;
import ir.sayandevelopment.database.MySQL;
import ir.sayandevelopment.database.Database;
import ir.sayandevelopment.listener.LoginListener;
import ir.sayandevelopment.listener.ServerConnectedListener;
import ir.sayandevelopment.listener.DisconnectListener;
import ir.sayandevelopment.listener.TabCompleteListener;
import net.minecrell.serverlistplus.core.ServerListPlusCore;
import net.minecrell.serverlistplus.core.player.PlayerIdentity;
import net.minecrell.serverlistplus.core.replacement.LiteralPlaceholder;
import net.minecrell.serverlistplus.core.replacement.ReplacementManager;
import net.minecrell.serverlistplus.core.status.StatusResponse;
import org.slf4j.Logger;

@Plugin(
        id = "sayanvanish",
        name = "SayanVanish",
        version = BuildConstants.VERSION,
        description = "Advanced vanish system",
        url = "syrent.ir",
        authors = {"Syrent"}
)
public class VelocityMain {

    private ProxyServer server;
    private Logger logger;
    public static VelocityMain INSTANCE;
    public static Gson GSON;
    public static MySQL SQL;
    public static final ChannelIdentifier SAYANVANISH_CHANNEL = MinecraftChannelIdentifier.create("sayanvanish", "main");
    public static final String PREFIX = "<gradient:#FF0000:#FF2A00>Vanish</gradient> <color:#555197>| ";
    public static final String PROXY_PREFIX = "<gradient:#FF0000:#FF2A00>Proxy</gradient> <color:#555197>| ";

    @Inject
    public VelocityMain(ProxyServer server, Logger logger) {
        INSTANCE = this;
        GSON = new Gson();

        this.server = server;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        SQL = new MySQL();

        try {
            logger.info("Connecting to SQL...");
            SQL.connect();
            SQL.init();
            logger.info("Connected to SQL.");
        } catch (Exception ex) {
            logger.error("Error while connecting to SQL.");
            ex.printStackTrace();
            return;
        }

        CommandMeta glistMeta = server.getCommandManager().metaBuilder("glist")
                .aliases("globallist").build();
        server.getCommandManager().register(glistMeta, new GListCommand());
        CommandMeta vanishMeta = server.getCommandManager().metaBuilder("vanish")
                .aliases("v").build();
        server.getCommandManager().register(vanishMeta, new VanishCommand());
        CommandMeta findMeta = server.getCommandManager().metaBuilder("find").build();
        server.getCommandManager().register(findMeta, new FindCommand());

        server.getEventManager().register(this, new TabCompleteListener());
        server.getEventManager().register(this, new LoginListener());
        server.getEventManager().register(this, new ServerConnectedListener());
        server.getEventManager().register(this, new DisconnectListener());

        ReplacementManager.getDynamic().add(new LiteralPlaceholder("%sayanvanish_total%") {
            @Override
            public String replace(StatusResponse response, String s) {
                PlayerIdentity identity = response.getRequest().getIdentity();
                if (identity != null) {
                    try {
                        return this.replace(s, server.getPlayerCount() - VelocityMain.SQL.vanishedCount());
                    } catch (Exception e) {
                        e.printStackTrace();
                        return this.replace(s, server.getPlayerCount());
                    }

                } else // Use the method below if player is unknown
                    return super.replace(response, s);
            }

            @Override
            public String replace(ServerListPlusCore core, String s) {
                // Unknown player, so let's just replace it with something unknown
                try {
                    return String.valueOf(server.getPlayerCount() - VelocityMain.SQL.vanishedCount());
                } catch (Exception e) {
                    e.printStackTrace();
                    return String.valueOf(server.getPlayerCount());
                }
            }
        });

        for (RegisteredServer registeredServer : server.getAllServers()) {
            String name = registeredServer.getServerInfo().getName();

            ReplacementManager.getDynamic().add(new LiteralPlaceholder("%sayanvanish_" + name.toLowerCase() + "%") {
                @Override
                public String replace(StatusResponse response, String s) {
                    PlayerIdentity identity = response.getRequest().getIdentity();
                    if (identity != null) {
                        try {
                            return this.replace(s, registeredServer.getPlayersConnected().size() - VelocityMain.SQL.vanishedCount(name.toLowerCase()));
                        } catch (Exception e) {
                            e.printStackTrace();
                            return this.replace(s, registeredServer.getPlayersConnected().size());
                        }

                    } else // Use the method below if player is unknown
                        return super.replace(response, s);
                }

                @Override
                public String replace(ServerListPlusCore core, String s) {
                    // Unknown player, so let's just replace it with something unknown
                    try {
                        return String.valueOf(registeredServer.getPlayersConnected().size() - VelocityMain.SQL.vanishedCount(name.toLowerCase()));
                    } catch (Exception e) {
                        e.printStackTrace();
                        return String.valueOf(registeredServer.getPlayersConnected().size());
                    }
                }
            });
        }
    }

    public ProxyServer getServer() {
        return server;
    }

    public Logger getLogger() {
        return logger;
    }
}
