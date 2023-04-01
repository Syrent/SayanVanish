package ir.syrent.velocityvanish.velocity.listener

import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyPingEvent
import com.velocitypowered.api.proxy.server.ServerPing
import ir.syrent.velocityvanish.velocity.VelocityVanish
import ir.syrent.velocityvanish.velocity.vruom.VRuom

class ProxyPingListener(
    private val plugin: VelocityVanish
) {

    init {
        VRuom.registerListener(this)
    }

    @Subscribe(order = PostOrder.EARLY)
    private fun onProxyPing(event: ProxyPingEvent) {
        val serverPing = event.ping
        if (serverPing.players.isPresent) {
            val onlinePlayers = serverPing.players.get().online
            val playersPing = ServerPing.Players(onlinePlayers - plugin.vanishedPlayersOnline().size, serverPing.asBuilder().maximumPlayers, serverPing.asBuilder().samplePlayers)
            event.ping = ServerPing(
                serverPing.version,
                playersPing,
                serverPing.descriptionComponent,
                serverPing.favicon.orElse(null)
            )
        }
    }
}