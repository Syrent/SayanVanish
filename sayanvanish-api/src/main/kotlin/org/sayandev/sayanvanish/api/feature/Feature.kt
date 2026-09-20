/*
 * This file is part of SayanVanish, licensed under the GNU General Public License v3.0.
 *
 * Copyright (c) 2026 Sayan Development and contributors
 *
 * SayanVanish is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SayanVanish is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.sayandev.sayanvanish.api.feature

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.sayandev.sayanvanish.api.Platform
import org.sayandev.sayanvanish.api.User
import org.sayandev.sayanvanish.api.feature.category.FeatureCategories
import org.sayandev.stickynote.core.configuration.Config
import java.io.File

@Serializable
@OptIn(ExperimentalSerializationApi::class)
abstract class Feature {
    abstract val id: String

    @Transient open var enabled: Boolean = true
    @Transient open val category: FeatureCategories = FeatureCategories.DEFAULT
    @Transient open val critical: Boolean = false

    @Transient open var condition: Boolean = true

    open fun isActive(): Boolean {
        return enabled && condition
    }

    open fun isActive(user: User): Boolean {
        return !user.hasPermission("sayanvanish.feature.disable.${id}") &&
            Features.isFeatureEnabled(user, this) &&
            isActive()
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
            val loadedFeature = createFromInstance(feature)
            loadedFeature.save()
            Features.addFeature(loadedFeature)
        }
    }
}
