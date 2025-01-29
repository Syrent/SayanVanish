package org.sayandev.sayanvanish.bukkit.config

import org.sayandev.sayanvanish.api.Platform
import org.sayandev.stickynote.bukkit.pluginDirectory
import org.sayandev.stickynote.core.configuration.Config
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import java.io.File
import java.util.*

public var settings: SettingsConfig = SettingsConfig.fromConfig() ?: SettingsConfig.defaultConfig()

@ConfigSerializable
data class SettingsConfig(
    val general: General = General(),
    val command: Command = Command(),
) : Config(
    pluginDirectory,
    fileName,
) {

    @ConfigSerializable
    data class General(
        val serverId: String = "${Platform.get().id}-${UUID.randomUUID()}",
        val language: String = LanguageConfig.Language.EN_US.id,
        val includePrefixInMessages: Boolean = true,
        val bstats: Boolean = true,
        val proxyMode: Boolean = false,
        val cacheUpdatePeriodTicks: Long = 20,
        val basicCacheUpdatePeriodTicks: Long = 20,
    )

    @ConfigSerializable
    data class Command(
        val name: String = "sayanvanish",
        val aliases: List<String> = listOf(
            "v",
            "vanish",
            "sv"
        )
    )

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