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
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent
import org.sayandev.sayanvanish.api.Permissions
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.paper.feature.ListenedFeature
import org.sayandev.stickynote.paper.plugin

@RegisteredFeature
@Serializable
@SerialName("register_permissions")
class FeatureRegisterPermissions: ListenedFeature() {

    @Transient override val id = "register_permissions"
    override var enabled: Boolean = true
    @Transient override val critical: Boolean = false
    @Transient var registeredPermissions = false

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        if (registeredPermissions) return
        for (i in 1..100) {
            player.hasPermission("${plugin.name}.level.${i}")
        }
        for (permission in Permissions.entries) {
            player.hasPermission(permission.permission())
        }

        registeredPermissions = true

        return
    }

}
