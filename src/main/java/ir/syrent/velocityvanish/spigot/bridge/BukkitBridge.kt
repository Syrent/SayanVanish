package ir.syrent.velocityvanish.spigot.bridge

import ir.syrent.velocityvanish.spigot.ruom.Ruom
import ir.syrent.velocityvanish.velocity.bridge.Bridge
import ir.syrent.velocityvanish.spigot.ruom.messaging.BukkitMessagingChannel
import org.bukkit.entity.Player

class BukkitBridge : Bridge, BukkitMessagingChannel("velocityvanish", "main") {

    override fun sendPluginMessage(sender: Any, messageByte: ByteArray) {
        if (sender !is Player) {
            throw IllegalArgumentException("Given object is not a bukkit player")
        }
        sender.sendPluginMessage(Ruom.plugin, "velocityvanish:main", messageByte)
    }

    override fun sendPluginMessage(messageByte: ByteArray) {
        throw IllegalStateException("Only proxies can send plugin message without player instances")
    }

}