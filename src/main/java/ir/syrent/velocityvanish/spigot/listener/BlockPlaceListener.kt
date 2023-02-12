package ir.syrent.velocityvanish.spigot.listener

import ir.syrent.velocityvanish.spigot.VelocityVanishSpigot
import ir.syrent.velocityvanish.spigot.storage.Settings
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent

class BlockPlaceListener(
    private val plugin: VelocityVanishSpigot
) : Listener {

    @EventHandler
    private fun onBlockBreak(event: BlockPlaceEvent) {
        val player = event.player
        if (plugin.vanishedNames.contains(player.name) && Settings.preventBlockPlace && !player.hasPermission("velocityvanish.bypass.prevention.block_place")) event.isCancelled = true
    }
}