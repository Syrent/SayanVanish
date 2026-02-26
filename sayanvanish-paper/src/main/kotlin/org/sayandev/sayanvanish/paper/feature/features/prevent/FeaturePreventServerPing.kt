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

import com.destroystokyo.paper.event.server.PaperServerListPingEvent
import kotlinx.serialization.SerialName
import org.bukkit.event.EventHandler
import org.sayandev.sayanvanish.api.VanishAPI
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.api.feature.category.FeatureCategories
import org.sayandev.sayanvanish.paper.api.PaperVanishUser.Companion.bukkitAdapt
import org.sayandev.sayanvanish.paper.feature.ListenedFeature
import org.sayandev.stickynote.bukkit.StickyNote
import org.sayandev.stickynote.bukkit.utils.ServerVersion
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@RegisteredFeature
@Serializable
@SerialName("prevent_server_ping")
class FeaturePreventServerPing: ListenedFeature() {

    @Transient override val id = "prevent_server_ping"
    override var enabled: Boolean = true
    @Transient override val category: FeatureCategories = FeatureCategories.PREVENTION

    @EventHandler
    private fun onPing(event: PaperServerListPingEvent) {
        if (!isActive()) return
        val vanishedPlayers = VanishAPI.get().getCacheService().getVanishUsers().getVanished().filter { it.bukkitAdapt().player() != null }
        event.numPlayers -= vanishedPlayers.count()
        if (StickyNote.isPaper && ServerVersion.supports(21)) {
            event.listedPlayers.removeIf { profile -> vanishedPlayers.map { vanishedPlayer -> vanishedPlayer.uniqueId }.contains(profile.id) }
        } else {
            event.playerSample.removeIf { profile -> vanishedPlayers.map { vanishedPlayer -> vanishedPlayer.uniqueId }.contains(profile.id) }
        }
    }

}