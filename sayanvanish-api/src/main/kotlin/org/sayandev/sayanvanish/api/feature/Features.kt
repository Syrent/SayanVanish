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

import org.sayandev.sayanvanish.api.User
import org.sayandev.sayanvanish.api.VanishAPI
import java.util.UUID

object Features {
    private fun registry(): FeatureRegistry {
        return VanishAPI.get().getFeatureRegistry()
    }

    @JvmStatic
    inline fun <reified T : Feature> getFeature(): T {
        return getFeature(T::class.java)
    }

    @JvmStatic
    fun <T : Feature> getFeature(type: Class<T>): T {
        return registry().findByType(type)
            ?: throw IllegalArgumentException("Feature `${type.simpleName}` is not registered.")
    }

    @JvmStatic
    fun getFeatureById(id: String): Feature? {
        return registry().findById(id)
    }

    @JvmStatic
    fun addFeature(feature: Feature): Boolean {
        return registry().add(feature)
    }

    @JvmStatic
    fun removeFeature(feature: Feature): Boolean {
        return registry().remove(feature)
    }

    @JvmStatic
    fun removeFeature(id: String): Boolean {
        return registry().remove(id)
    }

    @JvmStatic
    fun clearFeatures() {
        registry().clear()
    }

    @JvmStatic
    fun features(): List<Feature> {
        return registry().all()
    }

    @JvmStatic
    fun userFeatures(user: User): List<Feature> {
        return features().filter { registry().isEnabledForUser(user, it) }
    }

    @JvmStatic
    fun isFeatureEnabled(user: User, feature: Feature): Boolean {
        return registry().isEnabledForUser(user, feature)
    }

    @JvmStatic
    fun setFeatureEnabled(user: User, feature: Feature, enabled: Boolean) {
        registry().setEnabledForUser(user, feature, enabled)
    }

    @JvmStatic
    fun resetUserFeatureState(uniqueId: UUID) {
        registry().resetUser(uniqueId)
    }

    @JvmStatic
    fun resetUserFeatureState(user: User) {
        resetUserFeatureState(user.uniqueId)
    }

    @JvmStatic
    fun resetAllUserFeatureStates() {
        registry().resetUsers()
    }

}
