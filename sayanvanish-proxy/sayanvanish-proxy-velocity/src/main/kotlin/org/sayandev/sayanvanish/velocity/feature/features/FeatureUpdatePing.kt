package org.sayandev.sayanvanish.velocity.feature.features

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyPingEvent
import com.velocitypowered.api.proxy.server.ServerPing
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.velocity.api.SayanVanishVelocityAPI
import org.sayandev.sayanvanish.velocity.feature.ListenedFeature
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import kotlin.collections.count
import kotlin.collections.filter
import kotlin.collections.map
import kotlin.collections.toTypedArray
import kotlin.jvm.optionals.getOrNull

@RegisteredFeature
@ConfigSerializable
class FeatureUpdatePing : ListenedFeature("update_ping") {

    @Subscribe
    fun onProxyPing(event: ProxyPingEvent) {
        if (!isActive()) return
        val pingPlayers = event.ping.players.getOrNull() ?: return
        val onlineVanishedPlayers = SayanVanishVelocityAPI.getInstance().getVanishedUsers().filter { it.isOnline }
        val nonVanishedPlayersCount = pingPlayers.online - onlineVanishedPlayers.count()
        val nonVanishedPlayersSample = pingPlayers.sample.filter { !onlineVanishedPlayers.map { it.username }.contains(it.name) }
        event.ping = event.ping
            .asBuilder()
            .onlinePlayers(nonVanishedPlayersCount)
            .samplePlayers(*nonVanishedPlayersSample.map { ServerPing.SamplePlayer(it.name, it.id) }.toTypedArray())
            .build()
    }
}