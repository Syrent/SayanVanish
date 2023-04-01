package ir.syrent.velocityvanish.velocity.listener

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.TabCompleteEvent
import ir.syrent.velocityvanish.velocity.VelocityVanish
import ir.syrent.velocityvanish.velocity.vruom.VRuom

class TabCompleteListener(
    private val plugin: VelocityVanish
) {

    init {
        VRuom.registerListener(this)
    }

    @Subscribe
    private fun onTabComplete(event: TabCompleteEvent) {
        if (event.player.hasPermission("velocityvanish.bypass.tabcompleter")) return
        
        event.suggestions.removeIf {
            plugin.vanishedPlayers.contains(it)
        }
    }
}