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
    @Transient val additionalSerializers: TypeSerializerCollection = TypeSerializerCollection.defaults(),
    val critical: Boolean = false
) : Config(
    directory(category),
    "${id}.yml",
    additionalSerializers
) {
    @Transient open var condition: Boolean = true

    open fun isActive(): Boolean {
        return enabled && condition
    }

    open fun isActive(user: BasicUser): Boolean {
        return !user.hasPermission("sayanvanish.feature.disable.${id}") && Features.userFeatures(user).find { it.id == this.id }?.enabled != false
    }

    open fun enable() {
        enabled = true
    }

    open fun disable() {
        if (critical) {
            onCriticalDisabled()
        }
        enabled = false
    }

    fun toggle() {
        if (enabled && condition) {
            disable()
        } else {
            enable()
        }
    }

    fun onCriticalDisabled() {
        Platform.get().logger.warning("the feature '$id' is critical and currently disabled. We strongly recommend re-enabling it to avoid potential unexpected behavior. (path: ${directory(category).path}/${id}.yml)")
    }

    companion object {
        fun directory(category: FeatureCategories) =
            when (category.directory) {
                null -> {
                    File(Platform.get().rootDirectory, "features")
                }
                else -> {
                    File(File(Platform.get().rootDirectory, "features"), category.directory)
                }
            }

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
            } else {
                instance.disable()
            }
            return instance
        }
    }
}
