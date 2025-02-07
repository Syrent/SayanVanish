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
    @Transient val critical: Boolean = false
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
        return !user.hasPermission("sayanvanish.feature.disable.${id}") && Features.userFeatures(user).find { it.id == this.id }?.enabled != false && isActive()
    }

    open fun enable() {
        enabled = true
    }

    fun disable() {
        disable(false)
    }

    open fun disable(reload: Boolean) {
        if (critical) {
            onCriticalDisabled(reload)
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
        onCriticalDisabled(false)
    }

    fun onCriticalDisabled(reload: Boolean) {
        if (!reload) {
            Platform.get().logger.warning("the feature '$id' is critical and currently disabled. We strongly recommend re-enabling it to avoid potential unexpected behavior. (path: ${directory(category).path}/${id}.yml)")
        }
    }

    fun loadAndRegister() {
        loadAndRegister(this)
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
            return createFromInstance(freshInstance)
        }

        fun createFromInstance(feature: Feature): Feature {
            val category = feature.category
            val instance = getConfigFromFile(File(
                if (category.directory == null) {
                    File(Platform.get().rootDirectory, "features")
                } else {
                    File(File(Platform.get().rootDirectory, "features"), category.directory)
                }, "${feature.id}.yml"),
                feature.additionalSerializers)?.get(feature::class.java) ?: feature
            if (instance.enabled) {
                instance.enable()
            } else {
                instance.disable()
            }
            return instance
        }

        @JvmStatic
        fun loadAndRegister(feature: Feature) {
            createFromInstance(feature)
            feature.save()
            Features.addFeature(feature)
        }
    }
}
