package ir.syrent.velocityvanish.velocity.bridge

import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.server.RegisteredServer
import ir.syrent.velocityvanish.velocity.vruom.VRuom
import ir.syrent.velocityvanish.velocity.vruom.messaging.VelocityMessagingChannel

class VelocityBridge: Bridge, VelocityMessagingChannel("velocityvanish", "main") {

    override fun sendPluginMessage(sender: Any, messageByte: ByteArray) {
        if (sender !is Player) {
            throw IllegalArgumentException("Given object is not a velocity player")
        }
        sender.sendPluginMessage(name, messageByte)
    }

    override fun sendPluginMessage(messageByte: ByteArray) {
        for (server in VRuom.getServer().allServers) {
            server.sendPluginMessage(name, messageByte)
        }
    }

    fun sendPluginMessage(messageByte: ByteArray, server: RegisteredServer) {
        server.sendPluginMessage(name, messageByte)
    }

}