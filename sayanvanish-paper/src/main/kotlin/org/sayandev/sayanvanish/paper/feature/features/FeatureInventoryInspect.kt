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
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.sayandev.sayanvanish.api.feature.Configurable
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.paper.api.SayanVanishPaperAPI.Companion.cachedVanishUser
import org.sayandev.sayanvanish.paper.feature.ListenedFeature
import org.sayandev.stickynote.bukkit.plugin
import java.util.*

@RegisteredFeature
@Serializable
@SerialName("inventory_inspect")
class FeatureInventoryInspect(
    @Configurable val modificationPermission: String = "${plugin.name}.features.inventory_inspect.modify"
): ListenedFeature() {

    @Transient override val id = "inventory_inspect"
    override var enabled: Boolean = true

    @Transient val playerInventoryMap = mutableListOf<UUID>()

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onInteractPlayer(event: PlayerInteractAtEntityEvent) {
        val player = event.player
        val user = player.cachedVanishUser() ?: return
        if (!isActive(user)) return
        if (player.openInventory.type != InventoryType.CRAFTING) return
        if (!user.isVanished) return
        val target = event.rightClicked as? Player ?: return

        player.openInventory(target.inventory)
        playerInventoryMap.add(player.uniqueId)
    }

    @EventHandler
    private fun onClickPlayerInventory(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val user = player.cachedVanishUser() ?: return
        if (!isActive(user)) return
        if (!playerInventoryMap.contains(player.uniqueId)) return
        if (player.hasPermission(modificationPermission)) return
        event.isCancelled = true
    }

    @EventHandler
    private fun onInventoryClose(event: InventoryCloseEvent) {
        val player = event.player as? Player ?: return
        playerInventoryMap.remove(player.uniqueId)
    }




}