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
package org.sayandev.sayanvanish.paper.feature.features

import kotlinx.serialization.SerialName
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.paper.api.PaperVanishUser.Companion.bukkitAdapt
import org.sayandev.sayanvanish.paper.api.SayanVanishPaperAPI.Companion.cachedVanishUser
import org.sayandev.sayanvanish.paper.feature.ListenedFeature
import org.sayandev.sayanvanish.paper.utils.PlayerUtils.sendPrefixComponent
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@RegisteredFeature
@Serializable
@SerialName("ride_entity")
class FeatureRideEntity(
    val leaveVehicleWhenOthersEnter: Boolean = true,
    val exitMessage: String = "<red>You have been removed from the vehicle. because someone else entered it.",
): ListenedFeature() {

    @Transient override val id = "ride_entity"
    override var enabled: Boolean = true

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun removeVanishedPassengerOnEnter(event: PlayerInteractEntityEvent) {
        if (!leaveVehicleWhenOthersEnter) return
        val targetEntity = event.rightClicked
        val vanishedPassengers = targetEntity.passengers
            .filterIsInstance<Player>()
            .mapNotNull { player -> player.cachedVanishUser() }
            .filter { it.isVanished }
            .mapNotNull { it.bukkitAdapt().player() }
        for (vanishedPassenger in vanishedPassengers) {
            vanishedPassenger.leaveVehicle()
            vanishedPassenger.sendPrefixComponent(exitMessage)
        }
    }

}