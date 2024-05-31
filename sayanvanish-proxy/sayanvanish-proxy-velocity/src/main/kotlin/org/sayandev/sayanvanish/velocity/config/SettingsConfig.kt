package org.sayandev.sayanvanish.velocity.config

import org.sayandev.sayanvanish.api.Platform
import org.sayandev.stickynote.core.configuration.Config
import org.sayandev.stickynote.lib.spongepowered.configurate.objectmapping.ConfigSerializable
import org.sayandev.stickynote.velocity.dataDirectory
import java.io.File
import java.util.UUID

public var settings: SettingsConfig = SettingsConfig.fromConfig() ?: SettingsConfig.defaultConfig()

@ConfigSerializable
class SettingsConfig(
    val general: General = General()
) : Config(
    dataDirectory.toFile(),
    fileName
) {

    init {
        load()
    }

    @ConfigSerializable
    data class General(
        val serverId: String = "${Platform.get().id}-${UUID.randomUUID()}",
        val purgeOnlineHistoryOnStartup: Boolean = true
    )

    companion object {
        private val fileName = "settings.yml"
        val settingsFile = File(dataDirectory.toFile(), fileName)

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