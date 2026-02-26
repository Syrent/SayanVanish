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
package org.sayandev.sayanvanish.paper.config

import com.charleskorn.kaml.YamlComment
import org.sayandev.sayanvanish.api.Platform
import org.sayandev.stickynote.bukkit.pluginDirectory
import org.sayandev.stickynote.core.configuration.Config
import kotlinx.serialization.Serializable
import java.io.File
import java.util.*

// TODO: switch to kotlinx-serialization for yaml too?
@Serializable
class Settings(
    @YamlComment(
    "Do NOT copy and paste the SayanVanish directory across multiple servers.",
    "The server-id is generated during the plugin's first startup.",
    "Duplicating this file could lead to synchronization issues.",
    "",
    "General settings for the plugin",
    )
    val general: General = General(),
    @YamlComment("Command settings for the plugin")
    val vanishCommand: Command = Command(),
) {

    @Serializable
    data class General(
        @YamlComment("Unique server identifier. used for server identification if proxy mode is not enabled!")
        val serverId: String = "${Platform.get().id}-${UUID.randomUUID()}",
        @YamlComment("Language name", "Note: By default, it only includes the `en_US` language.", "However, you can create and add your own custom languages.")
        val language: String = LanguageConfig.Language.EN_US.id,
        @YamlComment("Whether to include prefix in messages, can be found in the language file.")
        val includePrefixInMessages: Boolean = true,
        @YamlComment("Enable or disable bStats metrics")
        val bstats: Boolean = true,
        @YamlComment(
        "If you want to synchronize the vanish status of players across multiple servers, enable this.",
        "You will also need to install the SayanVanish proxy plugin on your proxy server.",
        "WARNING: You need to use MySQL or Redis as the database for this feature to work properly.",
        )
        // TODO: Make a pinger or something to detect if the proxy mode is enabled or not
        val proxyMode: Boolean = false,
        @YamlComment("Cache update period in ticks. low values may cause performance issues.")
        val cacheUpdatePeriodTicks: Long = 20,
        @YamlComment("Basic cache update period in ticks. low values may cause performance issues.")
        val basicCacheUpdatePeriodTicks: Long = 20,
    )

    @Serializable
    data class Command(
        @YamlComment("Name of the main command")
        val name: String = "vanish",
        @YamlComment("Aliases for the main command")
        val aliases: List<String> = listOf(
            "v",
            "sayanvanish",
            "sv"
        )
    )

    fun serverId(): String {
        // TODO: use proxy server name on proxy servers
        return general.serverId
    }

    fun save() {
        Config.save(settingsFile, this)
    }

    companion object {
        private const val FILE_NAME = "settings.yml"

        val settingsFile = File(pluginDirectory, FILE_NAME)
        var config = fromConfig() ?: defaultConfig()

        @JvmStatic
        fun defaultConfig(): Settings {
            return Settings().also { it.save() }
        }

        @JvmStatic
        fun fromConfig(): Settings? {
            return Config.fromFile<Settings>(settingsFile)
        }

        @JvmStatic
        fun reload() {
            config = fromConfig() ?: defaultConfig()
        }

        @JvmStatic
        fun get(): Settings {
            return config
        }
    }
}