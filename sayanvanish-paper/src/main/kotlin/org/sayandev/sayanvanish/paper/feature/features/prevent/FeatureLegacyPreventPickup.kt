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
package org.sayandev.sayanvanish.paper.feature.features.prevent

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerPickupItemEvent
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.api.feature.category.FeatureCategories
import org.sayandev.sayanvanish.paper.api.SayanVanishPaperAPI.Companion.cachedVanishUser
import org.sayandev.sayanvanish.paper.feature.ListenedFeature
import org.sayandev.stickynote.bukkit.utils.ServerVersion

@RegisteredFeature
@Serializable
@SerialName("legacy_prevent_pickup")
class FeatureLegacyPreventPickup: ListenedFeature() {

    @Transient override val id = "legacy_prevent_pickup"
    override var enabled: Boolean = true
    @Transient override val category: FeatureCategories = FeatureCategories.PREVENTION

    override var condition: Boolean = !ServerVersion.supports(9)

    @EventHandler
    @Suppress("DEPRECATION")
    private fun onPickupItem(event: PlayerPickupItemEvent) {
        val user = event.player.cachedVanishUser() ?: return
        if (!isActive(user)) return
        if (user.isVanished) {
            event.isCancelled = true
        }
    }
}