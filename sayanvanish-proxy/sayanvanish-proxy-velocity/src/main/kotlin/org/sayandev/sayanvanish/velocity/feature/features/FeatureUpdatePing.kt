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