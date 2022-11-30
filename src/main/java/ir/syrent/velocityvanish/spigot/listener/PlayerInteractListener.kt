package ir.syrent.velocityvanish.spigot.listener

import ir.syrent.velocityvanish.spigot.VelocityVanishSpigot
import ir.syrent.velocityvanish.spigot.ruom.Ruom
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

class PlayerInteractListener(
    private val plugin: VelocityVanishSpigot
) : Listener {

    init {
        Ruom.registerListener(this)
    }

    @EventHandler
    private fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = event.player

        if (!plugin.vanishedNames.contains(player.name)) return
        if (event.action == Action.PHYSICAL || event.clickedBlock != null || !event.clickedBlock?.type?.name.equals("BIG_DRIPLEAF")) return

        event.isCancelled = true
    }
}