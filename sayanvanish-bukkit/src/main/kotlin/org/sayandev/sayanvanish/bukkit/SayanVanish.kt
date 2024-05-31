package org.sayandev.sayanvanish.bukkit

import com.alessiodp.libby.BukkitLibraryManager
import com.alessiodp.libby.Library
import org.bukkit.plugin.java.JavaPlugin
import org.sayandev.sayanvanish.api.Platform
import org.sayandev.sayanvanish.api.database.DatabaseMethod
import org.sayandev.sayanvanish.api.database.databaseConfig
import org.sayandev.sayanvanish.api.database.sql.SQLConfig
import org.sayandev.sayanvanish.api.feature.Features
import org.sayandev.sayanvanish.api.feature.RegisteredFeatureHandler
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI
import org.sayandev.sayanvanish.bukkit.command.SayanVanishCommand
import org.sayandev.sayanvanish.bukkit.config.LanguageConfig
import org.sayandev.sayanvanish.bukkit.config.SettingsConfig
import org.sayandev.sayanvanish.bukkit.config.settings
import org.sayandev.stickynote.bukkit.StickyNote
import org.sayandev.stickynote.bukkit.WrappedStickyNotePlugin
import org.sayandev.stickynote.bukkit.pluginDirectory
import org.sayandev.stickynote.bukkit.runAsync
import org.sayandev.stickynote.core.configuration.Config

open class SayanVanish : JavaPlugin() {

    override fun onEnable() {
        downloadLibraries()
        WrappedStickyNotePlugin(this)

        Platform.setAndRegister(Platform("bukkit", logger, pluginDirectory))

        SayanVanishBukkitAPI(databaseConfig.useCacheWhenAvailable)

        SettingsConfig
        if (settings.general.proxyMode && databaseConfig.method == DatabaseMethod.SQL && databaseConfig.sql.method == SQLConfig.SQLMethod.SQLITE) {
            error("The `proxy-mode` is enabled, but the database method is set to SQLite, which might lead to unexpected results. If you're using proxies such as Velocity or BungeeCord, make sure to use a different database method, such as MySQL or Redis.")
        }

        LanguageConfig

        VanishManager

        RegisteredFeatureHandler.process()

        SayanVanishCommand()

        runAsync({
            SayanVanishBukkitAPI.getInstance().database.updateBasicCache()
        }, 100, 100)
    }

    override fun onDisable() {
        SayanVanishBukkitAPI.getInstance().database.disconnect()
        StickyNote.shutdown()
    }

    private fun downloadLibraries() {
        logger.info("Trying to download required libraries, make sure your machine is connected to internet.")
        val libraryManager = BukkitLibraryManager(this)
        libraryManager.addMavenLocal()
        libraryManager.addRepository("https://repo.sayandev.org/snapshots")
        libraryManager.loadLibrary(
            Library.builder()
                .groupId("org{}sayandev")
                .artifactId("stickynote-core")
                .version("1.0.31")
                .relocate("org{}sayandev{}stickynote", "org{}sayandev{}sayanvanish{}lib{}stickynote")
                .build()
        )
        libraryManager.loadLibrary(
            Library.builder()
                .groupId("org{}sayandev")
                .artifactId("stickynote-bukkit")
                .version("1.0.31")
                .relocate("org{}sayandev{}stickynote", "org{}sayandev{}sayanvanish{}lib{}stickynote")
                .build()
        )
        libraryManager.loadLibrary(
            Library.builder()
                .groupId("org{}sayandev")
                .artifactId("stickynote-bukkit-nms")
                .version("1.0.31")
                .relocate("org{}sayandev{}stickynote", "org{}sayandev{}sayanvanish{}lib{}stickynote")
                .build()
        )
    }


}