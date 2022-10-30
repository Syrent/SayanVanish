package ir.syrent.velocityvanish.spigot.listener

import com.comphenix.protocol.wrappers.EnumWrappers
import ir.syrent.velocityvanish.spigot.VelocityVanishSpigot
import ir.syrent.velocityvanish.spigot.ruom.Ruom
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerTeleportEvent

class PlayerTeleportListener(
    private val plugin: VelocityVanishSpigot
) : Listener {

    init {
        Ruom.registerListener(this)
    }

    @EventHandler
    private fun onPlayerTeleport(event: PlayerTeleportEvent) {
        if (event.from.world == event.to?.world) return

        for (vanishedPlayer in plugin.vanishedNames.mapNotNull { Bukkit.getPlayerExact(it) }) {
            plugin.vanishManager.updateTabState(vanishedPlayer, EnumWrappers.NativeGameMode.SPECTATOR)
        }
    }
}