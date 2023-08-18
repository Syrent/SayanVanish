package ir.syrent.velocityvanish.spigot.listener

import io.papermc.lib.PaperLib
import ir.syrent.velocityvanish.spigot.VelocityVanishSpigot
import ir.syrent.velocityvanish.spigot.ruom.Ruom
import ir.syrent.velocityvanish.spigot.storage.Settings
import ir.syrent.velocityvanish.spigot.utils.ServerVersion
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerAdvancementDoneEvent

class PlayerAdvancementDoneListener(
    private val plugin: VelocityVanishSpigot
) : Listener {

    init {
        Ruom.registerListener(this)
    }

    @EventHandler
    private fun onPlayerAdvancementDone(event: PlayerAdvancementDoneEvent) {
        if (PaperLib.isPaper() && Settings.preventAdvancement) {
            val player = event.player
            if (!plugin.vanishedNames.contains(player.name)) return
            val advancement = event.advancement
            event.message(null)
            for (criteria in advancement.criteria) {
                player.getAdvancementProgress(advancement).revokeCriteria(criteria)
            }
        }
    }

}