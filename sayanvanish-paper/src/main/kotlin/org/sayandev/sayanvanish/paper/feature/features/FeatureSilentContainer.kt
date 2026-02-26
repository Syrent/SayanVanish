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

import com.cryptomorin.xseries.XMaterial
import kotlinx.serialization.SerialName
import org.bukkit.GameMode
import org.bukkit.block.Container
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.inventory.CraftingInventory
import org.bukkit.util.Vector
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.paper.api.SayanVanishPaperAPI.Companion.cachedVanishUser
import org.sayandev.sayanvanish.paper.api.event.PaperUserUnVanishEvent
import org.sayandev.sayanvanish.paper.feature.ListenedFeature
import org.sayandev.stickynote.bukkit.runSync
import org.sayandev.stickynote.bukkit.utils.ServerVersion
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.util.*

@RegisteredFeature
@Serializable
@SerialName("silent_container")
class FeatureSilentContainer: ListenedFeature() {

    @Transient override val id = "silent_container"
    override var enabled: Boolean = true

    @Transient override var condition: Boolean = ServerVersion.supports(13)
    @Transient private val containerPlayersData = mutableMapOf<UUID, ContainerPlayerData>()

    /*override fun enable() {
        // https://github.com/Syrent/SayanVanish/issues/356
        if (hasPlugin("Multiverse-Inventories")) {
            warn("`silent_container` feature is not compatible with Multiverse-Inventories. Disabling the feature.")
            return
        }

        super.enable()
    }*/

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = event.player
        val user = player.cachedVanishUser() ?: return
        if (!isActive(user)) return
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        val clickedBlock = event.clickedBlock ?: return
        if (!user.isVanished) return
        if (player.gameMode == GameMode.SPECTATOR) return

        if (clickedBlock.type == XMaterial.ENDER_CHEST.get()!!) {
            event.isCancelled = true
            player.openInventory(player.enderChest)
            return
        }

        if (clickedBlock.state !is Container) return

        containerPlayersData[player.uniqueId] = ContainerPlayerData(player.gameMode, player.allowFlight, player.isFlying)

        player.allowFlight = true
        player.isFlying = true
        player.velocity = Vector(0.0, 0.0, 0.0)
        player.gameMode = GameMode.SPECTATOR

        runSync({
            containerPlayersData[player.uniqueId]?.apply(player)
            containerPlayersData.remove(player.uniqueId)
        }, 1)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onTeleport(event: PlayerTeleportEvent) {
        val player = event.player
        // Allow spectators to teleport via their hotbar
        if (event.cause == PlayerTeleportEvent.TeleportCause.SPECTATE) return
        if (!containerPlayersData.contains(player.uniqueId) && event.cause != PlayerTeleportEvent.TeleportCause.SPECTATE) return
        event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private fun onUnVanish(event: PaperUserUnVanishEvent) {
        val player = event.user.player()
        if (player != null) {
            containerPlayersData[player.uniqueId]?.apply(player)
        }

        containerPlayersData.remove(event.user.uniqueId)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onQuit(event: PlayerQuitEvent) {
        containerPlayersData[event.player.uniqueId]?.apply(event.player)
        containerPlayersData.remove(event.player.uniqueId)
    }

    @EventHandler
    private fun onInventoryClose(event: InventoryCloseEvent) {
        val player = event.player as? Player ?: return
        val user = player.cachedVanishUser() ?: return
        if (!user.isVanished) return
        if (event.inventory is CraftingInventory) return

        containerPlayersData[player.uniqueId] = ContainerPlayerData(player.gameMode, player.allowFlight, player.isFlying)

        player.allowFlight = true
        player.isFlying = true
        player.velocity = Vector(0.0, 0.0, 0.0)
        player.gameMode = GameMode.SPECTATOR

        runSync({
            containerPlayersData[player.uniqueId]?.apply(player)
            containerPlayersData.remove(player.uniqueId)
        }, 1)
    }

    data class ContainerPlayerData(
        val gameMode: GameMode,
        val allowFlight: Boolean,
        val isFlying: Boolean,
    ) {
        fun apply(player: Player) {
            player.gameMode = gameMode
            player.allowFlight = allowFlight
            player.isFlying = isFlying
        }
    }

}