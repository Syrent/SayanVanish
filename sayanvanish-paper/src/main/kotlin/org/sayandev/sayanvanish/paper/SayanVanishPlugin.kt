/*
 * This file is part of SayanVanish, licensed under the GNU General Public License v3.0.
 *
 * Copyright (c) 2026 Sayan Development and contributors
 *
 * SayanVanish is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SayanVanish is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.sayandev.sayanvanish.paper

import kotlinx.coroutines.runBlocking
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.sayandev.sayanvanish.api.Platform
import org.sayandev.sayanvanish.api.SayanVanishAPI
import org.sayandev.sayanvanish.api.VanishAPI
import org.sayandev.sayanvanish.api.storage.DatabaseType
import org.sayandev.sayanvanish.api.storage.StorageConfig
import org.sayandev.sayanvanish.api.storage.sql.SQLConfig
import org.sayandev.sayanvanish.paper.api.Metrics
import org.sayandev.sayanvanish.paper.command.SayanVanishCommand
import org.sayandev.sayanvanish.paper.config.Settings
import org.sayandev.sayanvanish.paper.config.language
import org.sayandev.stickynote.command.bukkit.CommandApiLifecycle
import org.sayandev.stickynote.bukkit.StickyNote
import org.sayandev.stickynote.bukkit.WrappedStickyNotePlugin
import org.sayandev.stickynote.bukkit.error
import java.io.File

class SayanVanishPlugin : JavaPlugin() {

    override fun onLoad() {
        WrappedStickyNotePlugin(this)
        CommandApiLifecycle.load(this)
    }

    override fun onEnable() {
        CommandApiLifecycle.enable()
        setInstance(this)

        if (!Platform.setAndRegister(PaperPlatform())) {
            Bukkit.getPluginManager().disablePlugin(this)
            return
        }

        Settings.reload()
        SayanVanishAPI.initialize(Settings.get().general.proxyMode)

        if (Settings.get().general.proxyMode && StorageConfig.get().method == DatabaseType.SQL && StorageConfig.get().sql.method == SQLConfig.SQLMethod.SQLITE) {
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
                    StorageConfig.get().method.name
                })
            }
        }
    }

    override fun onDisable() {
        runBlocking {
            Platform.get().unregister()
        }
        CommandApiLifecycle.disable()
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
