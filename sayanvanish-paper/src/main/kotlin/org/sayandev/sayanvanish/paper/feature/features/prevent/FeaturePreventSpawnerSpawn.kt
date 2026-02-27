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

import com.destroystokyo.paper.event.entity.PreSpawnerSpawnEvent
import kotlinx.serialization.SerialName
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.api.feature.category.FeatureCategories
import org.sayandev.sayanvanish.paper.api.SayanVanishPaperAPI.Companion.cachedVanishUser
import org.sayandev.sayanvanish.paper.feature.ListenedFeature
import org.sayandev.stickynote.paper.StickyNote
import org.sayandev.stickynote.paper.onlinePlayers
import org.sayandev.stickynote.paper.utils.ServerVersion
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@RegisteredFeature
@Serializable
@SerialName("prevent_spawner_spawn")
class FeaturePreventSpawnerSpawn: ListenedFeature() {

    @Transient override val id = "prevent_spawner_spawn"
    override var enabled: Boolean = true
    @Transient override val category: FeatureCategories = FeatureCategories.PREVENTION

    @Transient
    override var condition: Boolean = StickyNote.isPaper && ServerVersion.supports(16)

    /**
    * @note This feature will be too performance intense if i want to check feature is active or not on a per-player basis
    * */
    @EventHandler
    private fun onPreSpawn(event: PreSpawnerSpawnEvent) {
        if (!isActive()) return
        val nearPlayers = onlinePlayers
            .filter { player -> player.gameMode != GameMode.SPECTATOR && player.world == event.spawnerLocation.world && player.location.distance(event.spawnerLocation) <= 256 }
        val allIsVanished = nearPlayers.all { it.cachedVanishUser()?.isVanished == true }
        if (allIsVanished) {
            event.isCancelled = true
        }
    }

}