package org.sayandev.sayanvanish.bukkit

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.sayandev.sayanvanish.api.Platform
import org.sayandev.sayanvanish.api.storage.DatabaseType
import org.sayandev.sayanvanish.api.storage.storageConfig
import org.sayandev.sayanvanish.api.storage.sql.SQLConfig
import org.sayandev.sayanvanish.api.storage.sql.SQLDatabase
import org.sayandev.sayanvanish.bukkit.api.Metrics
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI
import org.sayandev.sayanvanish.bukkit.command.SayanVanishCommand
import org.sayandev.sayanvanish.bukkit.config.language
import org.sayandev.sayanvanish.bukkit.config.settings
import org.sayandev.sayanvanish.bukkit.health.HealthCheckRequestPublisher
import org.sayandev.sayanvanish.bukkit.health.ServerInfoPublisher
import org.sayandev.stickynote.bukkit.StickyNote
import org.sayandev.stickynote.bukkit.error
import org.sayandev.stickynote.bukkit.pluginDirectory
import org.sayandev.stickynote.bukkit.runAsync
import org.sayandev.stickynote.loader.bukkit.StickyNoteBukkitLoader
import java.io.File

public lateinit var sayanvanish: SayanVanish

open class SayanVanish : JavaPlugin() {

    override fun onEnable() {
        StickyNoteBukkitLoader(this)
        sayanvanish = this

        if (!Platform.setAndRegister(Platform("bukkit", logger, pluginDirectory, settings.general.serverId))) {
            Bukkit.getPluginManager().disablePlugin(this)
            return
        }

        SayanVanishBukkitAPI()

        HealthCheckRequestPublisher
        ServerInfoPublisher

        settings
        if (settings.general.proxyMode && storageConfig.method == DatabaseType.SQL && storageConfig.sql.method == SQLConfig.SQLMethod.SQLITE) {
            error("The `proxy-mode` is enabled, but the database method is set to SQLite, which might lead to unexpected results. If you're using proxies such as Velocity or BungeeCord, make sure to use a different database method, such as MySQL or Redis.")
        }

        language

        VanishManager

        SayanVanishCommand()

        runAsync({
            SayanVanishBukkitAPI.getInstance().database.getUsersAsync { users ->
                SayanVanishBukkitAPI.getInstance().database.cache = users.associateBy { it.uniqueId }.toMutableMap()
                SayanVanishAPI.getDatabase().cache = users.associateBy { it.uniqueId }.toMutableMap()
            }
        }, 0, settings.general.cacheUpdatePeriodTicks)

        runAsync({
            if (storageConfig.method == DatabaseType.SQL) {
                SayanVanishBukkitAPI.getInstance().database.getBasicUsersAsync { users ->
                    (SayanVanishBukkitAPI.getInstance().database as SQLDatabase).basicCache = users.associateBy { it.uniqueId }.toMutableMap()
                    (SayanVanishAPI.getDatabase() as SQLDatabase).basicCache = users.associateBy { it.uniqueId }.toMutableMap()
                }
            }
        }, 0, settings.general.basicCacheUpdatePeriodTicks)

        if (settings.general.bstats) {
            Metrics(this, 23914).apply {
                this.addCustomChart(Metrics.SingleLineChart("vanished") {
                    SayanVanishBukkitAPI.getInstance().getVanishedUsers().count()
                })
                this.addCustomChart(Metrics.SimplePie("proxied") {
                    if (settings.general.proxyMode) "On Proxy" else "No Proxy"
                })
                this.addCustomChart(Metrics.SimplePie("database_method") {
                    storageConfig.method.name
                })
            }
        }
    }

    override fun onDisable() {
        SayanVanishBukkitAPI.getInstance().database.disconnect()
        StickyNote.shutdown()
    }

    fun pluginFile(): File {
        return this.file
    }

}
