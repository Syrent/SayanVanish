package ir.syrent.velocityvanish.spigot.listener

import ir.syrent.velocityvanish.spigot.VelocityVanishSpigot
import ir.syrent.velocityvanish.spigot.ruom.Ruom
import ir.syrent.velocityvanish.spigot.storage.Settings
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerPickupItemEvent

class PlayerItemPickupListener(
    private val plugin: VelocityVanishSpigot
) : Listener {

    init {
        Ruom.registerListener(this)
    }

    @EventHandler
    @Suppress("DEPRECATION")
    private fun onPlayerItemPickup(event: PlayerPickupItemEvent) {
        if (Settings.preventPickup) {
            if (plugin.vanishedNames.contains(event.player.name)) {
                event.isCancelled = true
            }
        }
    }
}