package ir.syrent.velocityvanish.spigot.listener

import ir.syrent.velocityvanish.spigot.VelocityVanishSpigot
import ir.syrent.velocityvanish.spigot.storage.Settings
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent

class BlockBreakListener(
    private val plugin: VelocityVanishSpigot
) : Listener {

    @EventHandler
    private fun onBlockBreak(event: BlockBreakEvent) {
        val player = event.player
        if (plugin.vanishedNames.contains(player.name) && Settings.preventBlockBreak) event.isCancelled = true
    }
}