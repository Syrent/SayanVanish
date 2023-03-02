package ir.syrent.velocityvanish.spigot.listener

import ir.syrent.velocityvanish.spigot.VelocityVanishSpigot
import ir.syrent.velocityvanish.spigot.ruom.Ruom
import ir.syrent.velocityvanish.spigot.storage.Settings
import ir.syrent.velocityvanish.spigot.utils.ServerVersion
import ir.syrent.velocityvanish.spigot.utils.Utils
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class PlayerQuitListener(
    private val plugin: VelocityVanishSpigot
) : Listener {

    init {
        Ruom.registerListener(this)
    }

    @EventHandler
    private fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player

        if (plugin.vanishedNames.contains(player.name)) {
            if (ServerVersion.supports(9)) {
                player.isInvulnerable = false
            }
            event.quitMessage = null
        }
        Utils.actionbarPlayers.remove(player)
    }

}