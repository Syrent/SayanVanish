package org.sayandev.sayanvanish.bungeecord

import net.md_5.bungee.api.plugin.Plugin
import org.sayandev.sayanvanish.api.Platform
import org.sayandev.sayanvanish.bungeecord.api.SayanVanishBungeeAPI
import org.sayandev.sayanvanish.proxy.config.settings
import org.sayandev.sayanvanish.velocity.VanishManager
import org.sayandev.stickynote.bungeecord.StickyNote
import org.sayandev.stickynote.bungeecord.dataDirectory
import org.sayandev.stickynote.bungeecord.registerListener
import org.sayandev.stickynote.bungeecord.server
import org.sayandev.stickynote.lib.libby.BungeeLibraryManager
import org.sayandev.stickynote.lib.libby.Library
import org.sayandev.stickynote.loader.bungee.StickyNoteBungeeLoader
import java.util.concurrent.TimeUnit

class SayanVanish : Plugin() {

    override fun onEnable() {
        downloadLibraries()

        StickyNoteBungeeLoader.load(this)
        Platform.setAndRegister(Platform("bungeecord", logger, dataDirectory, settings.general.serverId))

        SayanVanishBungeeAPI()

        registerListener(VanishManager)

        if (settings.general.purgeOnlineHistoryOnStartup) {
            for (onlineServer in server.servers) {
                SayanVanishBungeeAPI.getInstance().database.purgeBasic(onlineServer.value.name)
            }
            SayanVanishBungeeAPI.getInstance().database.purgeBasic(settings.general.serverId)
        }

        StickyNote.run({
            SayanVanishBungeeAPI.getInstance().database.updateBasicCacheAsync()
        }, 5, 5, TimeUnit.SECONDS)

        StickyNote.run({
            SayanVanishBungeeAPI.getInstance().database.updateCacheAsync()
        }, 1, 1, TimeUnit.SECONDS)
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
    }
}