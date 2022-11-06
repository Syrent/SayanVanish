package ir.syrent.velocityvanish.spigot.listener

import ir.syrent.velocityvanish.spigot.VelocityVanishSpigot
import ir.syrent.velocityvanish.spigot.ruom.Ruom
import me.sayandevelopment.sayanchat.api.event.PlayerMentionEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class PlayerMentionListener(
    private val plugin: VelocityVanishSpigot
) : Listener {

    init {
        Ruom.registerListener(this)
    }

    @EventHandler
    private fun onPlayerMention(event: PlayerMentionEvent) {
        val player = event.mentioned

        if (plugin.vanishedNames.contains(player.name)) {
            event.isCancelled = true
        }
    }
}