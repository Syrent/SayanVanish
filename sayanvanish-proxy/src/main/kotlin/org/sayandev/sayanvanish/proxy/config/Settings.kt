package org.sayandev.sayanvanish.proxy.config

import org.sayandev.sayanvanish.api.Platform
import org.sayandev.stickynote.core.configuration.Config
import kotlinx.serialization.Serializable
import com.charleskorn.kaml.YamlComment
import java.io.File
import java.util.UUID

@Serializable
class Settings(
    @YamlComment("Do NOT copy and paste the SayanVanish directory across multiple servers.",
    "The server-id is generated during the plugin's first startup.",
    "Duplicating this file could lead to synchronization issues.",
    "",
    "General settings for the plugin")
    val general: General = General(),
    @YamlComment("Command settings for the plugin")
    val command: Command = Command()
) {

    @Serializable
    data class General(
        @YamlComment("Unique server identifier. doesn't do anything special yet.")
        val serverId: String = "${Platform.get().id}-${UUID.randomUUID()}",
        @YamlComment("Language name",
        "Note: By default, it only includes the `en_US` language.",
        "However, you can create and add your own custom languages.")
        val language: String = LanguageConfig.Language.EN_US.id,
        @YamlComment("Weather to purge online history of users on startup.")
        val purgeOnlineHistoryOnStartup: Boolean = true,
        val purgeUsersOnStartup: Boolean = true,
        @YamlComment("Cache update period in milliseconds. low values may cause performance issues.")
        val cacheUpdatePeriodMillis: Long = 300,
        @YamlComment("Basic cache update period in milliseconds. low values may cause performance issues.")
        val basicCacheUpdatePeriodMillis: Long = 5000,
        @YamlComment("Whether to include prefix in messages, can be found in the language file.")
        val includePrefixInMessages: Boolean = true,
    )

    @Serializable
    data class Command(
        @YamlComment("Name of the main command")
        val name: String = "sayanvanishproxy",
        @YamlComment("Aliases for the main command")
        val aliases: List<String> = listOf(
            "vp",
            "vanishp",
            "svp"
        )
    )

    fun save() {
        Config.save(settingsFile, this)
    }

    companion object {
        private const val FILE_NAME = "settings.yml"

        val settingsFile = File(Platform.get().rootDirectory, FILE_NAME)
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