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
package org.sayandev.sayanvanish.velocity.feature.features

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyPingEvent
import com.velocitypowered.api.proxy.server.ServerPing
import kotlinx.serialization.SerialName
import org.sayandev.sayanvanish.api.VanishAPI
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.velocity.feature.ListenedFeature
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.jvm.optionals.getOrNull

@RegisteredFeature
@Serializable
@SerialName("update_ping")
class FeatureUpdatePing : ListenedFeature() {

    @Transient override val id = "update_ping"

    @Subscribe
    fun onProxyPing(event: ProxyPingEvent) {
        if (!isActive()) return
        val pingPlayers = event.ping.players.getOrNull() ?: return
        val vanishedOnlineUsers = VanishAPI.get().getCacheService().getVanishUsers().values.filter { vanishUser -> vanishUser.isVanished && vanishUser.isOnline }
        val nonVanishedPlayersCount = pingPlayers.online - vanishedOnlineUsers.size
        val nonVanishedPlayersSample = pingPlayers.sample.filter { !vanishedOnlineUsers.map { it.username }.contains(it.name) }
        event.ping = event.ping
            .asBuilder()
            .onlinePlayers(nonVanishedPlayersCount)
            .samplePlayers(*nonVanishedPlayersSample.map { ServerPing.SamplePlayer(it.name, it.id) }.toTypedArray())
            .build()
    }
}