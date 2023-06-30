package ir.syrent.velocityvanish.spigot.listener

import ir.syrent.velocityvanish.spigot.VelocityVanishSpigot
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.ServerListPingEvent

class ServerListPingListener(
    private val plugin: VelocityVanishSpigot
) : Listener {

    @EventHandler
    private fun onServerListPing(event: ServerListPingEvent) {
//        val response = ServerboundPingRequestPacket(0).handle(ServerStatusPacketListenerImpl)
//        ServerboundPingRequestPacket
    }

}