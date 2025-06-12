package org.sayandev.sayanvanish.bukkit.config

import org.sayandev.sayanvanish.api.Platform
import org.sayandev.stickynote.bukkit.pluginDirectory
import org.sayandev.stickynote.core.configuration.Config
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment
import java.io.File
import java.util.*

public var settings: SettingsConfig = SettingsConfig.fromConfig() ?: SettingsConfig.defaultConfig()

@ConfigSerializable
class SettingsConfig(
    @Comment("""
    Do NOT copy and paste the SayanVanish directory across multiple servers.
    The server-id is generated during the plugin's first startup.
    Duplicating this file could lead to synchronization issues.
    
    General settings for the plugin
    """)
    val general: General = General(),
    @Comment("Command settings for the plugin")
    val vanishCommand: Command = Command(),
) : Config(
    pluginDirectory,
    fileName,
) {

    @ConfigSerializable
    data class General(
        @Comment("Unique server identifier. used for server identification if proxy mode is not enabled!")
        val serverId: String = "${Platform.get().id}-${UUID.randomUUID()}",
        @Comment("""
        Language name
        Note: By default, it only includes the `en_US` language.
        However, you can create and add your own custom languages.
        """)
        val language: String = LanguageConfig.Language.EN_US.id,
        @Comment("Whether to include prefix in messages, can be found in the language file.")
        val includePrefixInMessages: Boolean = true,
        @Comment("Enable or disable bStats metrics")
        val bstats: Boolean = true,
        @Comment("""
        If you want to synchronize the vanish status of players across multiple servers, enable this.
        You will also need to install the SayanVanish proxy plugin on your proxy server.
        WARNING: You need to use MySQL or Redis as the database for this feature to work properly.
        """)
        val proxyMode: Boolean = false,
        @Comment("Cache update period in ticks. low values may cause performance issues.")
        val cacheUpdatePeriodTicks: Long = 20,
        @Comment("Basic cache update period in ticks. low values may cause performance issues.")
        val basicCacheUpdatePeriodTicks: Long = 20,
    )

    @ConfigSerializable
    data class Command(
        @Comment("Name of the main command")
        val name: String = "vanish",
        @Comment("Aliases for the main command")
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

    companion object {
        private val fileName = "settings.yml"
        val settingsFile = File(pluginDirectory, fileName)

        @JvmStatic
        fun defaultConfig(): SettingsConfig {
            return SettingsConfig().also { it.save() }
        }

        @JvmStatic
        fun fromConfig(): SettingsConfig? {
            return fromConfig<SettingsConfig>(settingsFile)
        }
    }
}