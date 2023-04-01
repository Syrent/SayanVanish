package ir.syrent.velocityvanish.velocity.vruom;

import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.nio.file.Path;

public class VRUoMPlugin {

    private static VRUoMPlugin instance;
    private static ProxyServer server;
    private static Logger logger;
    private static Path dataDirectory;

    public VRUoMPlugin(ProxyServer server, Logger logger) {
        instance = this;
        VRUoMPlugin.server = server;
        VRUoMPlugin.logger = logger;
    }

    public VRUoMPlugin(ProxyServer server, Logger logger, Path dataDirectory) {
        instance = this;
        VRUoMPlugin.server = server;
        VRUoMPlugin.logger = logger;
        VRUoMPlugin.dataDirectory = dataDirectory;
    }

    public static VRUoMPlugin get() {
        return instance;
    }

    public static ProxyServer getServer() {
        return server;
    }

    public static Logger getLogger() {
        return logger;
    }

    public static Path getDataDirectory() {
        return dataDirectory;
    }
}