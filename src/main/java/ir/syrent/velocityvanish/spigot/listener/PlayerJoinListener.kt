package ir.syrent.velocityvanish.spigot.listener

import com.comphenix.protocol.wrappers.EnumWrappers
import ir.syrent.velocityvanish.spigot.VelocityVanishSpigot
import ir.syrent.velocityvanish.spigot.ruom.Ruom
import ir.syrent.velocityvanish.spigot.storage.Settings
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PlayerJoinListener(
    private val plugin: VelocityVanishSpigot
) : Listener {

    init {
        Ruom.registerListener(this)
    }

    @EventHandler
    private fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player

        for (vanishedPlayer in plugin.vanishedNames.mapNotNull { Bukkit.getPlayerExact(it) }) {
            plugin.vanishManager.hidePlayer(vanishedPlayer)
            plugin.vanishManager.updateTabState(vanishedPlayer, EnumWrappers.NativeGameMode.SPECTATOR)
        }

        if (!Settings.remember) return

        if (plugin.vanishedNames.contains(player.name)) {
            plugin.vanishManager.vanish(player)
            event.joinMessage = null
        } else {
            plugin.vanishManager.unVanish(player)
        }
    }

}