package org.sayandev.sayanvanish.bukkit.config

import org.sayandev.sayanvanish.api.feature.Feature
import org.sayandev.sayanvanish.api.feature.Features
import org.sayandev.stickynote.bukkit.pluginDirectory
import org.sayandev.stickynote.core.configuration.Config
import org.sayandev.stickynote.lib.spongepowered.configurate.objectmapping.ConfigSerializable
import org.sayandev.stickynote.lib.spongepowered.configurate.serialize.TypeSerializerCollection
import java.io.File

public var settings: SettingsConfig = SettingsConfig.fromConfig() ?: SettingsConfig.defaultConfig()

@ConfigSerializable
data class SettingsConfig(
    val general: General = General(),
//    val vanish: Vanish = Vanish(),
) : Config(
    pluginDirectory,
    fileName,
//    typeSerializerCollection
) {

    init {
        load()
    }

    @ConfigSerializable
    data class General(
        val language: String = LanguageConfig.Language.EN_US.id,
        val updateChecker: Boolean = true,
        val proxyMode: Boolean = false
    )

    @ConfigSerializable
    data class Vanish(
//        val seeAsSpectator: Boolean = true, // TODO
//        val effects: List<String> = listOf(PotionEffectType.NIGHT_VISION.key.key), // TODO
        // Prevent:
//            val playerTablistPackets: Boolean = true, // TODO
//            val containerOpenPacket: Boolean = true, // TODO
        val features: MutableList<Feature> = Features.features,
    )

    companion object {
        /*private val typeSerializerCollection = TypeSerializerCollection.builder()
            .apply {
                register(Feature::class.java, FeatureTypeSerializer())
            }
            .build()*/

        private val fileName = "settings.yml"
        val settingsFile = File(pluginDirectory, fileName)

        @JvmStatic
        fun defaultConfig(): SettingsConfig {
            return SettingsConfig()
        }

        @JvmStatic
        fun fromConfig(): SettingsConfig? {
            return fromConfig<SettingsConfig>(settingsFile/*, typeSerializerCollection*/)
        }
    }
}