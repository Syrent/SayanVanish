package org.sayandev.sayanvanish.bukkit

import org.bukkit.plugin.java.JavaPlugin
import org.sayandev.sayanvanish.api.Platform
import org.sayandev.sayanvanish.api.database.DatabaseMethod
import org.sayandev.sayanvanish.api.database.databaseConfig
import org.sayandev.sayanvanish.api.database.sql.SQLConfig
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI
import org.sayandev.sayanvanish.bukkit.command.SayanVanishCommand
import org.sayandev.sayanvanish.bukkit.config.LanguageConfig
import org.sayandev.sayanvanish.bukkit.config.SettingsConfig
import org.sayandev.sayanvanish.bukkit.config.settings
import org.sayandev.stickynote.bukkit.*
import org.sayandev.stickynote.loader.bukkit.StickyNoteBukkitLoader

open class SayanVanish : JavaPlugin() {

    override fun onEnable() {
        StickyNoteBukkitLoader.load(this)

        Platform.setAndRegister(Platform("bukkit", logger, pluginDirectory, settings.general.serverId))

        SayanVanishBukkitAPI()

        SettingsConfig
        if (settings.general.proxyMode && databaseConfig.method == DatabaseMethod.SQL && databaseConfig.sql.method == SQLConfig.SQLMethod.SQLITE) {
            error("The `proxy-mode` is enabled, but the database method is set to SQLite, which might lead to unexpected results. If you're using proxies such as Velocity or BungeeCord, make sure to use a different database method, such as MySQL or Redis.")
        }

        LanguageConfig

        VanishManager

        SayanVanishCommand()

        runAsync({
            SayanVanishBukkitAPI.getInstance().database.updateCacheAsync()
        }, 0, 100)

        runAsync({
            SayanVanishBukkitAPI.getInstance().database.updateBasicCacheAsync()
        }, 0, 20)
    }

    override fun onDisable() {
        SayanVanishBukkitAPI.getInstance().database.disconnect()
        StickyNote.shutdown()
    }

}