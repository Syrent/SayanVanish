package org.sayandev.sayanvanish.api.feature

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.sayandev.sayanvanish.api.Platform
import org.sayandev.sayanvanish.api.User
import org.sayandev.sayanvanish.api.feature.category.FeatureCategories
import org.sayandev.stickynote.core.configuration.Config
import java.io.File

@Serializable
abstract class Feature(
    @Transient open val id: String = "@transient",
    open var enabled: Boolean,
    @Transient open val category: FeatureCategories = FeatureCategories.DEFAULT,
    @Transient open val critical: Boolean = false
) {

    @Transient open var condition: Boolean = true

    open fun isActive(): Boolean {
        return enabled && condition
    }

    open fun isActive(user: User): Boolean {
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

    fun save() {
        Config.save(File(directory(category), "${id}.yml"), this, Yaml(Platform.get().serializers, Config.yaml.configuration))
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

        inline fun <reified T : Feature> createFromInstance(feature: T): T {
            val category = feature.category
            val instance = Config.fromFile<T>(File(
                if (category.directory == null) {
                    File(Platform.get().rootDirectory, "features")
                } else {
                    File(File(Platform.get().rootDirectory, "features"), category.directory)
                }, "${feature.id}.yml"), Platform.get().serializers) ?: feature
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
