package org.sayandev.sayanvanish.velocity.feature.features

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyPingEvent
import com.velocitypowered.api.proxy.server.ServerPing
import org.sayandev.sayanvanish.api.feature.Configurable
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.velocity.api.SayanVanishVelocityAPI
import org.sayandev.sayanvanish.velocity.feature.ListenedFeature
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment
import kotlin.jvm.optionals.getOrNull

@RegisteredFeature
@ConfigSerializable
class FeatureUpdatePing(
    @Comment("List of server names whose players are counted toward the online player count. If empty, players from all servers will be counted.")
    @Configurable val servers: List<String> = emptyList()
) : ListenedFeature("update_ping") {

    @Subscribe
    fun onProxyPing(event: ProxyPingEvent) {
        if (!isActive()) return
        val pingPlayers = event.ping.players.getOrNull() ?: return
        val vanishedOnlineUsers = SayanVanishVelocityAPI.getInstance().database.getUsers().filter { user -> user.isVanished && user.isOnline && (servers.isEmpty() || user.serverId in servers) }
        val vanishedOnlineUsersNames = vanishedOnlineUsers.map { vanishUser -> vanishUser.username }.toSet()
        val nonVanishedPlayersCount = SayanVanishVelocityAPI.getInstance().database.getBasicUsers(true).filter { basicUser ->
            (servers.isEmpty() || basicUser.serverId in servers) &&
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