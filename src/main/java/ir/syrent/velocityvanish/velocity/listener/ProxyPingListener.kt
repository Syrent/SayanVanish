package ir.syrent.velocityvanish.velocity.listener

import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyPingEvent
import com.velocitypowered.api.proxy.server.ServerPing
import ir.syrent.velocityvanish.velocity.VelocityVanish
import me.mohamad82.ruom.VRuom

class ProxyPingListener(
    private val plugin: VelocityVanish
) {

    init {
        VRuom.registerListener(this)
    }

    @Subscribe
    private fun onProxyPing(event: ProxyPingEvent) {
        val serverPing = event.ping
        if (serverPing.players.isPresent) {
            val onlinePlayers = serverPing.players.get().online
            val playersPing = ServerPing.Players(onlinePlayers - plugin.vanishedPlayers.size, serverPing.asBuilder().maximumPlayers, serverPing.asBuilder().samplePlayers)
            event.ping = ServerPing(
                serverPing.version,
                playersPing,
                serverPing.descriptionComponent,
                serverPing.favicon.orElse(null)
            )
        }
    }
}