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
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockReceiveGameEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.sayandev.sayanvanish.api.feature.Configurable
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.api.feature.category.FeatureCategories
import org.sayandev.sayanvanish.paper.api.SayanVanishPaperAPI.Companion.cachedVanishUser
import org.sayandev.sayanvanish.paper.feature.ListenedFeature
import org.sayandev.stickynote.paper.utils.ServerVersion
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@RegisteredFeature
@Serializable
@SerialName("prevent_sculk")
class FeaturePreventSculk(
    @Configurable val preventSculkSensor: Boolean = true,
    @Configurable val preventShriek: Boolean = true
): ListenedFeature() {

    @Transient override val id = "prevent_sculk"
    override var enabled: Boolean = true
    @Transient override val category: FeatureCategories = FeatureCategories.PREVENTION

    val sculkBlocks = listOf(
        "SCULK_SENSOR",
        "CALIBRATED_SCULK_SENSOR",
        "SCULK_SHRIEKER"
    )

    @Transient
    override var condition: Boolean = ServerVersion.supports(19)

    @EventHandler(priority = EventPriority.HIGH)
    private fun cancelSculkSensor(event: BlockReceiveGameEvent) {
        if (!preventSculkSensor) return
        val player = event.entity as? Player ?: return
        val user = player.cachedVanishUser() ?: return
        if (!user.isVanished) return
        if (!isActive(user)) return
        event.isCancelled = true
    }

    @EventHandler
    private fun cancelShriek(event: PlayerInteractEvent) {
        if (!preventShriek) return
        val block = event.clickedBlock ?: return
        if (event.action != Action.PHYSICAL) return
        if (!sculkBlocks.contains(block.type.name)) return
        val player = event.player
        val user = player.cachedVanishUser() ?: return
        if (!user.isVanished) return
        if (!isActive(user)) return
        event.isCancelled = true
    }

}