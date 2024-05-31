package org.sayandev.sayanvanish.api.feature

import org.sayandev.sayanvanish.api.Platform
import org.sayandev.sayanvanish.api.feature.category.FeatureCategories
import org.sayandev.stickynote.core.configuration.Config
import org.sayandev.stickynote.lib.spongepowered.configurate.objectmapping.ConfigSerializable
import org.sayandev.stickynote.lib.spongepowered.configurate.serialize.TypeSerializerCollection
import java.io.File

@ConfigSerializable
abstract class Feature(
    val id: String,
    var enabled: Boolean = true,
    val category: FeatureCategories = FeatureCategories.DEFAULT,
    @Transient val additionalSerializers: TypeSerializerCollection = TypeSerializerCollection.defaults()
) : Config(
    when (category.directory) {
        null -> {
            File(Platform.get().rootDirectory, "features")
        }
        else -> {
            File(File(Platform.get().rootDirectory, "features"), category.directory)
        }
    },
    "${id}.yml",
    additionalSerializers
) {

    @Transient open var condition: Boolean = true

    open fun isActive(): Boolean {
        return enabled && condition
    }

    open fun enable() {
        enabled = true
    }

    open fun disable() {
        enabled = false
    }

    companion object {
        fun createFromConfig(type: Class<out Feature>): Feature {
            val freshInstance = type.getDeclaredConstructor().newInstance()
            val instance = getConfigFromFile(File(File(Platform.get().rootDirectory, "features"), "${freshInstance.id}.yml"), freshInstance.additionalSerializers)?.get(type) ?: freshInstance
            if (instance.enabled) {
                instance.enable()
            }
            return instance
        }
    }
}
