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
package org.sayandev.sayanvanish.paper

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.sayandev.sayanvanish.api.VanishAPI
import org.sayandev.sayanvanish.paper.api.PaperVanishUser.Companion.bukkitAdapt
import org.sayandev.sayanvanish.paper.api.SayanVanishPaperAPI.Companion.getCachedOrCreateUser
import org.sayandev.sayanvanish.paper.api.SayanVanishPaperAPI.Companion.getOrAddUser
import org.sayandev.sayanvanish.paper.config.Settings
import org.sayandev.stickynote.paper.launch
import org.sayandev.stickynote.paper.registerListener

object VanishManager : Listener {

    init {
        registerListener(this)
    }

    @EventHandler
    private fun addUserOnJoin(event: PlayerJoinEvent) {
        if (Settings.get().general.proxyMode) return

        val player = event.player
        launch {
            player.getOrAddUser()
        }
    }

    @EventHandler
    private fun makeUserOfflineOnQuit(event: PlayerQuitEvent) {
        if (Settings.get().general.proxyMode) return

        val player = event.player

        launch {
            val user = player.getCachedOrCreateUser()
            user.isOnline = false
            user.saveAndSync()
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun hideVanishedPlayersForJoinedPlayer(event: PlayerJoinEvent) {
        for (vanishedUser in VanishAPI.get().getCacheService().getVanishUsers().getVanished().map { it.bukkitAdapt() }.filter { it.player() != null }) {
            vanishedUser.hideFor(event.player)
        }
    }

}
