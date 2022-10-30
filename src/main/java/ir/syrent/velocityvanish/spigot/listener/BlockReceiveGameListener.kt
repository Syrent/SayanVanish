package ir.syrent.velocityvanish.spigot.listener

import ir.syrent.velocityvanish.spigot.VelocityVanishSpigot
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockReceiveGameEvent

class BlockReceiveGameListener(
    private val plugin: VelocityVanishSpigot
) : Listener {

    @EventHandler
    private fun onBlockReceiveGameEvent(event: BlockReceiveGameEvent) {
        val entity = event.entity

        if (entity !is Player) return
        if (!plugin.vanishedNames.contains(entity.name)) return

        event.isCancelled = true
    }
}