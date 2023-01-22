package ir.syrent.velocityvanish.spigot.listener

import ir.syrent.velocityvanish.spigot.VelocityVanishSpigot
import ir.syrent.velocityvanish.spigot.event.PostUnVanishEvent
import ir.syrent.velocityvanish.spigot.ruom.Ruom
import ir.syrent.velocityvanish.spigot.storage.Message
import ir.syrent.velocityvanish.spigot.storage.Settings
import ir.syrent.velocityvanish.spigot.utils.Utils
import ir.syrent.velocityvanish.utils.TextReplacement
import ir.syrent.velocityvanish.utils.component
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class PostUnVanishListener(
    private val plugin: VelocityVanishSpigot
) : Listener {

    init {
        Ruom.registerListener(this)
    }

    @EventHandler
    private fun onPostUnVanish(event: PostUnVanishEvent) {
        val player = event.player
        Utils.sendVanishActionbar(player)
        plugin.vanishedNames.remove(player.name)
        plugin.bridgeManager?.updateVanishedPlayersRequest(player, false)

        val joinMessage = Settings.formatMessage(player, Message.JOIN_MESSAGE, TextReplacement("player", player.name), TextReplacement("play_displayname", player.displayName))
        if (joinMessage.isNotBlank() && joinMessage.isNotEmpty() && event.sendJoinMessage) {
            Ruom.broadcast(joinMessage.component())
        }
    }
}