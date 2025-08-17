package org.sayandev.sayanvanish.velocity.feature.features

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyPingEvent
import com.velocitypowered.api.proxy.server.ServerPing
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.velocity.api.SayanVanishVelocityAPI
import org.sayandev.sayanvanish.velocity.feature.ListenedFeature
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import kotlin.jvm.optionals.getOrNull

@RegisteredFeature
@ConfigSerializable
class FeatureUpdatePing : ListenedFeature("update_ping") {

    @Subscribe
    fun onProxyPing(event: ProxyPingEvent) {
        if (!isActive()) return
        val pingPlayers = event.ping.players.getOrNull() ?: return
        val vanishedOnlineUsers = SayanVanishVelocityAPI.getInstance().database.getUsers().filter { user -> user.isVanished && user.isOnline }
        val vanishedOnlineUsersNames = vanishedOnlineUsers.map { vanishUser -> vanishUser.username }
        val nonVanishedPlayersCount = SayanVanishVelocityAPI.getInstance().database.getBasicUsers(true).filter { basicUser ->
            !vanishedOnlineUsersNames.contains(basicUser.username)
        }.size
        val nonVanishedPlayersSample = pingPlayers.sample.filter { pingPlayer ->
            !vanishedOnlineUsersNames.contains(pingPlayer.name)
        }
        event.ping = event.ping
            .asBuilder()
            .onlinePlayers(nonVanishedPlayersCount)
            .samplePlayers(*nonVanishedPlayersSample.map { ServerPing.SamplePlayer(it.name, it.id) }.toTypedArray())
            .build()
    }
}