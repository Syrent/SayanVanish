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
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.Locale

interface FeatureRegistry {
    fun add(feature: Feature): Boolean
    fun remove(feature: Feature): Boolean
    fun remove(id: String): Boolean
    fun clear()
    fun all(): List<Feature>
    fun findById(id: String): Feature?
    fun <T : Feature> findByType(type: Class<T>): T?
    fun isEnabledForUser(user: User, feature: Feature): Boolean
    fun setEnabledForUser(user: User, feature: Feature, enabled: Boolean)
    fun resetUser(userId: UUID)
    fun resetUsers()
}

class DefaultFeatureRegistry : FeatureRegistry {
    private val features = CopyOnWriteArrayList<Feature>()
    private val userDisabledFeatures = ConcurrentHashMap<UUID, MutableSet<String>>()

    private fun normalize(id: String): String {
        return id.lowercase(Locale.ROOT)
    }

    override fun add(feature: Feature): Boolean {
        if (features.any { it.id.equals(feature.id, ignoreCase = true) }) {
            return false
        }
        features.add(feature)
        return true
    }

    override fun remove(feature: Feature): Boolean {
        return remove(feature.id)
    }

    override fun remove(id: String): Boolean {
        val normalizedId = normalize(id)
        val removed = features.removeIf { it.id.equals(id, ignoreCase = true) }
        if (removed) {
            userDisabledFeatures.values.forEach { it.remove(normalizedId) }
        }
        return removed
    }

    override fun clear() {
        features.clear()
        userDisabledFeatures.clear()
    }

    override fun all(): List<Feature> {
        return features.toList()
    }

    override fun findById(id: String): Feature? {
        return features.firstOrNull { it.id.equals(id, ignoreCase = true) }
    }

    override fun <T : Feature> findByType(type: Class<T>): T? {
        @Suppress("UNCHECKED_CAST")
        return features.firstOrNull { type.isInstance(it) } as? T
    }

    override fun isEnabledForUser(user: User, feature: Feature): Boolean {
        val disabled = userDisabledFeatures[user.uniqueId] ?: return true
        return normalize(feature.id) !in disabled
    }

    override fun setEnabledForUser(user: User, feature: Feature, enabled: Boolean) {
        val disabled = userDisabledFeatures.computeIfAbsent(user.uniqueId) { ConcurrentHashMap.newKeySet() }
        val featureId = normalize(feature.id)
        if (enabled) {
            disabled.remove(featureId)
        } else {
            disabled.add(featureId)
        }
        if (disabled.isEmpty()) {
            userDisabledFeatures.remove(user.uniqueId)
        }
    }

    override fun resetUser(userId: UUID) {
        userDisabledFeatures.remove(userId)
    }

    override fun resetUsers() {
        userDisabledFeatures.clear()
    }
}
