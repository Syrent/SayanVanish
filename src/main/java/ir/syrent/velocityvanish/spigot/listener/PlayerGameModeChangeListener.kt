package ir.syrent.velocityvanish.spigot.listener

import ir.syrent.velocityvanish.spigot.VelocityVanishSpigot
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerGameModeChangeEvent

class PlayerGameModeChangeListener(
    private val plugin: VelocityVanishSpigot
) : Listener {

    @EventHandler
    private fun onAfkStatusChange(event: PlayerGameModeChangeEvent) {
        val player = event.player
        if (!plugin.vanishedNames.contains(player.name)) return

        plugin.vanishManager.updateTabState(player, GameMode.SPECTATOR)
    }

}