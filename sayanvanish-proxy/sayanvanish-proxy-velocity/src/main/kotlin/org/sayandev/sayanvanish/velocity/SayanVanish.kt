package org.sayandev.sayanvanish.velocity

import com.alessiodp.libby.Library
import com.alessiodp.libby.VelocityLibraryManager
import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import org.sayandev.sayanvanish.BuildConstants
import org.sayandev.sayanvanish.api.Platform
import org.sayandev.sayanvanish.proxy.config.settings
import org.sayandev.sayanvanish.velocity.api.SayanVanishVelocityAPI
import org.sayandev.stickynote.velocity.WrappedStickyNotePlugin
import org.sayandev.stickynote.velocity.registerListener
import org.slf4j.LoggerFactory
import java.nio.file.Path
import java.util.logging.Logger

class SayanVanish @Inject constructor(
    val server: ProxyServer,
    val logger: Logger,
    @DataDirectory val dataDirectory: Path
) {

    @Subscribe
    fun onProxyInitialize(event: ProxyInitializeEvent) {
        downloadLibraries()

        WrappedStickyNotePlugin(this, PLUGIN_ID, server, logger, dataDirectory).initialize()
        Platform.setAndRegister(Platform("velocity", logger, dataDirectory.toFile()))

        SayanVanishVelocityAPI()

        registerListener(VanishManager)

        if (settings.general.purgeOnlineHistoryOnStartup) {
            for (onlineServer in server.allServers) {
                SayanVanishVelocityAPI.getInstance().database.purgeBasic(onlineServer.serverInfo.name)
            }
            SayanVanishVelocityAPI.getInstance().database.purgeBasic(settings.general.serverId)
        }
    }

    private fun downloadLibraries() {
        logger.info("Trying to download required libraries, make sure your machine is connected to internet.")
        val libraryManager = VelocityLibraryManager(this, LoggerFactory.getLogger(this::class.java), dataDirectory, server.pluginManager)
        libraryManager.addMavenLocal()
        libraryManager.addMavenCentral()
        libraryManager.addRepository("https://repo.sayandev.org/snapshots")
        try {
            Class.forName("com.mysql.cj.jdbc.Driver")
        } catch (_: Exception) {
            libraryManager.loadLibrary(
                Library.builder()
                    .groupId("com{}mysql")
                    .artifactId("mysql-connector-j")
                    .version("8.4.0")
                    .build()
            )
        }
        try {
            Class.forName("org.xerial.sqlite-jdbc")
        } catch (_: Exception) {
            libraryManager.loadLibrary(
                Library.builder()
                    .groupId("org{}xerial")
                    .artifactId("sqlite-jdbc")
                    .version("3.46.0.0")
                    .build()
            )
        }
        libraryManager.loadLibrary(
            Library.builder()
                .groupId("org{}sayandev")
                .artifactId("stickynote-core")
                .version(BuildConstants.STICKYNOTE_VERSION)
                .relocate("org{}sayandev{}stickynote", "org{}sayandev{}sayanvanish{}lib{}stickynote")
                .build()
        )
        libraryManager.loadLibrary(
            Library.builder()
                .groupId("org{}sayandev")
                .artifactId("stickynote-proxy-velocity")
                .version(BuildConstants.STICKYNOTE_VERSION)
                .relocate("org{}sayandev{}stickynote", "org{}sayandev{}sayanvanish{}lib{}stickynote")
                .build()
        )
    }

    companion object {
        const val PLUGIN_ID = "sayanvanish"
    }

}