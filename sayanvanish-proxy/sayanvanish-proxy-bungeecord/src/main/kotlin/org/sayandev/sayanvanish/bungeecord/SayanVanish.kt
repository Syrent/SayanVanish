package org.sayandev.sayanvanish.bungeecord

import com.alessiodp.libby.BungeeLibraryManager
import com.alessiodp.libby.Library
import net.md_5.bungee.api.plugin.Plugin
import org.sayandev.sayanvanish.api.Platform
import org.sayandev.sayanvanish.api.database.databaseConfig
import org.sayandev.sayanvanish.bungeecord.api.SayanVanishBungeeAPI
import org.sayandev.sayanvanish.proxy.config.settings
import org.sayandev.sayanvanish.velocity.VanishManager
import org.sayandev.stickynote.bungeecord.WrappedStickyNotePlugin
import org.sayandev.stickynote.bungeecord.dataDirectory
import org.sayandev.stickynote.bungeecord.registerListener
import org.sayandev.stickynote.bungeecord.server

class SayanVanish : Plugin() {

    override fun onEnable() {
        downloadLibraries()

        WrappedStickyNotePlugin(this).initialize()
        Platform.setAndRegister(Platform("bungeecord", logger, dataDirectory))

        SayanVanishBungeeAPI()

        registerListener(VanishManager)

        if (settings.general.purgeOnlineHistoryOnStartup) {
            for (onlineServer in server.servers) {
                SayanVanishBungeeAPI.getInstance().database.purgeBasic(onlineServer.value.name)
            }
            SayanVanishBungeeAPI.getInstance().database.purgeBasic(settings.general.serverId)
        }
    }

    private fun downloadLibraries() {
        proxy.logger.info("Trying to download required libraries, make sure your machine is connected to internet.")
        val libraryManager = BungeeLibraryManager(this)
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
                .version("1.0.32")
                .relocate("org{}sayandev{}stickynote", "org{}sayandev{}sayanvanish{}lib{}stickynote")
                .build()
        )
        libraryManager.loadLibrary(
            Library.builder()
                .groupId("org{}sayandev")
                .artifactId("stickynote-proxy-bungeecord")
                .version("1.0.32")
                .relocate("org{}sayandev{}stickynote", "org{}sayandev{}sayanvanish{}lib{}stickynote")
                .build()
        )
    }
}