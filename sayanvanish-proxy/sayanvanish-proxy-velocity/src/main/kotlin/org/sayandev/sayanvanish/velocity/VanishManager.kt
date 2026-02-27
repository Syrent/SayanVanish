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
package org.sayandev.sayanvanish.velocity

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.player.ServerPostConnectEvent
import kotlinx.coroutines.awaitAll
import org.sayandev.sayanvanish.api.VanishAPI
import org.sayandev.sayanvanish.velocity.api.SayanVanishVelocityAPI.Companion.getOrCreateVanishUser
import org.sayandev.sayanvanish.velocity.api.VelocityVanishUser.Companion.getVanishUser
import org.sayandev.sayanvanish.velocity.api.VelocityVanishUser.Companion.velocityAdapt
import org.sayandev.sayanvanish.velocity.event.VelocityUserUnVanishEvent
import org.sayandev.sayanvanish.velocity.event.VelocityUserVanishEvent
import org.sayandev.stickynote.velocity.launch
import org.sayandev.stickynote.velocity.server
import kotlin.jvm.optionals.getOrNull

object VanishManager {

    @Subscribe
    private fun onPostLogin(event: ServerPostConnectEvent) {
        val player = event.player ?: return
        launch {
            val user = player.getOrCreateVanishUser().velocityAdapt()
            player.currentServer.getOrNull()?.serverInfo?.name?.takeUnless { it.isEmpty() }?.let { serverName ->
                user.serverId = serverName
            }
            user.isOnline = true
            user.saveAndSync().awaitAll()

            if (user.isVanished) {
                server.eventManager.fireAndForget(VelocityUserVanishEvent(user, user.currentOptions))
            } else {
                server.eventManager.fireAndForget(VelocityUserUnVanishEvent(user, user.currentOptions))
            }
        }
    }

    @Subscribe
    private fun onDisconnect(event: DisconnectEvent) {
        val player = event.player ?: return
        launch {
            player.getVanishUser()?.velocityAdapt()?.let { user ->
                user.isOnline = false
                user.saveAndSync().awaitAll()
            }
            VanishAPI.get().getDatabase().removeUser(player.uniqueId).await()
        }
    }

}
