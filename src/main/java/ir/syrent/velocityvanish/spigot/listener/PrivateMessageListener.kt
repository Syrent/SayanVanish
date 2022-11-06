package ir.syrent.velocityvanish.spigot.listener

import ir.syrent.velocityvanish.spigot.VelocityVanishSpigot
import ir.syrent.velocityvanish.spigot.ruom.Ruom
import me.sayandevelopment.sayanchat.api.event.PrivateMessageSendEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class PrivateMessageListener(
    private val plugin: VelocityVanishSpigot
) : Listener {

    init {
        Ruom.registerListener(this)
    }

    @EventHandler
    private fun onPrivateMessage(event: PrivateMessageSendEvent) {
        val receiverName = event.receiver

        if (plugin.vanishedNames.contains(receiverName)) {
            event.isCancelled = true
        }
    }
}