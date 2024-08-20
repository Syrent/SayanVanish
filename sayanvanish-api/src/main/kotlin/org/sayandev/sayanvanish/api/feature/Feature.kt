package org.sayandev.sayanvanish.api.feature

import org.sayandev.sayanvanish.api.BasicUser
import org.sayandev.sayanvanish.api.Platform
import org.sayandev.sayanvanish.api.feature.category.FeatureCategories
import org.sayandev.stickynote.core.configuration.Config
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import java.io.File

@ConfigSerializable
abstract class Feature(
    val id: String,
    var enabled: Boolean = true,
    @Transient val category: FeatureCategories = FeatureCategories.DEFAULT,
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

    open fun isActive(user: BasicUser): Boolean {
        return enabled && condition && Features.userFeatures(user).find { it.id == this.id }?.enabled != false
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
            val category = freshInstance.category
            val instance = getConfigFromFile(File(
                if (category.directory == null) {
                    File(Platform.get().rootDirectory, "features")
                } else {
                    File(File(Platform.get().rootDirectory, "features"), category.directory)
                }, "${freshInstance.id}.yml"),
                freshInstance.additionalSerializers)?.get(type) ?: freshInstance
            if (instance.enabled) {
                instance.enable()
            }
            return instance
        }
    }
}
