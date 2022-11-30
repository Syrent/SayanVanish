package ir.syrent.velocityvanish.spigot.listener

import ir.syrent.velocityvanish.spigot.VelocityVanishSpigot
import ir.syrent.velocityvanish.spigot.ruom.Ruom
import ir.syrent.velocityvanish.spigot.storage.Settings
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent

class EntityDamageListener(
    private val plugin: VelocityVanishSpigot
) : Listener {

    init {
        Ruom.registerListener(this)
    }

    @EventHandler
    private fun onEntityDamage(event: EntityDamageEvent) {
        val entity = event.entity
        if (entity !is Player) return

        if (Settings.invincible) {
            if (plugin.vanishedNames.contains(entity.name)) {
                event.isCancelled = true
            }
        }

    }

}