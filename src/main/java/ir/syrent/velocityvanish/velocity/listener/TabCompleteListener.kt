package ir.syrent.velocityvanish.velocity.listener

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.TabCompleteEvent
import ir.syrent.velocityvanish.velocity.VelocityVanish
import me.mohamad82.ruom.VRuom

class TabCompleteListener(
    private val plugin: VelocityVanish
) {

    init {
        VRuom.registerListener(this)
    }

    @Subscribe
    private fun onTabComplete(event: TabCompleteEvent) {
        event.suggestions.removeIf {
            plugin.vanishedPlayers.contains(it)
        }
    }
}