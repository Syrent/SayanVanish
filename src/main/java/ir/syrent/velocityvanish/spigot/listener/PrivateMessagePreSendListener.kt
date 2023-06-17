package ir.syrent.velocityvanish.spigot.listener

import ir.syrent.velocityvanish.spigot.VelocityVanishSpigot
import ir.syrent.velocityvanish.spigot.ruom.Ruom
import ir.syrent.velocityvanish.spigot.storage.Settings
import net.ess3.api.events.PrivateMessagePreSendEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class PrivateMessagePreSendListener(
    private val plugin: VelocityVanishSpigot
) : Listener {

    init {
        Ruom.registerListener(this)
    }

    @EventHandler
    private fun onPrivateMessage(event: PrivateMessagePreSendEvent) {
        if (!Settings.forcePreventPrivateMessages) return

        val receiver = event.recipient
        val sender = event.sender

        if (plugin.vanishedNames.contains(receiver.name)) {
            sender.sendMessage(com.earth2me.essentials.I18n.tl("errorWithMessage", com.earth2me.essentials.I18n.tl("playerNotFound")))
            event.isCancelled = true
        }
    }
}