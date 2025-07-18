package org.sayandev.sayanvanish.bukkit

import kotlinx.coroutines.runBlocking
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.sayandev.sayanvanish.api.Platform
import org.sayandev.sayanvanish.api.VanishAPI
import org.sayandev.sayanvanish.api.storage.DatabaseType
import org.sayandev.sayanvanish.api.storage.sql.SQLConfig
import org.sayandev.sayanvanish.api.storage.storageConfig
import org.sayandev.sayanvanish.bukkit.api.Metrics
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI
import org.sayandev.sayanvanish.bukkit.command.SayanVanishCommand
import org.sayandev.sayanvanish.bukkit.config.Settings
import org.sayandev.sayanvanish.bukkit.config.language
import org.sayandev.stickynote.bukkit.StickyNote
import org.sayandev.stickynote.bukkit.error
import org.sayandev.stickynote.loader.bukkit.StickyNoteBukkitLoader
import java.io.File

class SayanVanishPlugin : JavaPlugin() {

    override fun onEnable() {
        StickyNoteBukkitLoader(this)
        setInstance(this)

        if (!Platform.setAndRegister(BukkitPlatform())) {
            Bukkit.getPluginManager().disablePlugin(this)
            return
        }

        SayanVanishBukkitAPI()

        Settings.reload()

        if (Settings.get().general.proxyMode && storageConfig.method == DatabaseType.SQL && storageConfig.sql.method == SQLConfig.SQLMethod.SQLITE) {
            error("The `proxy-mode` is enabled, but the database method is set to SQLite, which might lead to unexpected results. If you're using proxies such as Velocity or BungeeCord, make sure to use a different database method, such as MySQL or Redis.")
        }

        language

        VanishManager

        SayanVanishCommand()

        if (Settings.get().general.bstats) {
            Metrics(this, 23914).apply {
                this.addCustomChart(Metrics.SingleLineChart("vanished") {
                    VanishAPI.get().getDatabase().getVanishUsersBlocking().filter { it.isVanished }.size
                })
                this.addCustomChart(Metrics.SimplePie("proxied") {
                    if (Settings.get().general.proxyMode) "On Proxy" else "No Proxy"
                })
                this.addCustomChart(Metrics.SimplePie("database_method") {
                    storageConfig.method.name
                })
            }
        }
    }

    override fun onDisable() {
        runBlocking {
            Platform.get().unregister()
        }
        StickyNote.shutdown()
    }

    fun pluginFile(): File {
        return this.file
    }

    companion object {
        private lateinit var instance: SayanVanishPlugin

        fun getInstance(): SayanVanishPlugin {
            return instance
        }

        private fun setInstance(plugin: SayanVanishPlugin) {
            instance = plugin
        }
    }

}
