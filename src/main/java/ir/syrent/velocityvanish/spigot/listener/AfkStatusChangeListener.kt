package ir.syrent.velocityvanish.spigot.listener

import ir.syrent.velocityvanish.spigot.VelocityVanishSpigot
import net.ess3.api.events.AfkStatusChangeEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class AfkStatusChangeListener(
    private val plugin: VelocityVanishSpigot
) : Listener {

    @EventHandler
    private fun onAfkStatusChange(event: AfkStatusChangeEvent) {
        if (!plugin.vanishedNames.contains(event.affected.name)) return

        event.isCancelled = true
    }

}