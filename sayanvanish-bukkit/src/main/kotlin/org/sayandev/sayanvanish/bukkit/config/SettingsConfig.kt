package org.sayandev.sayanvanish.bukkit.config

import org.sayandev.sayanvanish.api.Platform
import org.sayandev.stickynote.bukkit.pluginDirectory
import org.sayandev.stickynote.core.configuration.Config
import org.sayandev.stickynote.lib.spongepowered.configurate.objectmapping.ConfigSerializable
import java.io.File
import java.util.*

public var settings: SettingsConfig = SettingsConfig.fromConfig() ?: SettingsConfig.defaultConfig()

@ConfigSerializable
data class SettingsConfig(
    val general: General = General(),
) : Config(
    pluginDirectory,
    fileName,
) {

    init {
        load()
    }

    @ConfigSerializable
    data class General(
        val serverId: String = "${Platform.get().id}-${UUID.randomUUID()}",
        val language: String = LanguageConfig.Language.EN_US.id,
        val proxyMode: Boolean = false
    )

    companion object {
        private val fileName = "settings.yml"
        val settingsFile = File(pluginDirectory, fileName)

        @JvmStatic
        fun defaultConfig(): SettingsConfig {
            return SettingsConfig()
        }

        @JvmStatic
        fun fromConfig(): SettingsConfig? {
            return fromConfig<SettingsConfig>(settingsFile)
        }
    }
}