package org.sayandev.sayanvanish.bukkit.feature.features.hook

import github.scarsz.discordsrv.DiscordSRV
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.sayandev.sayanvanish.api.feature.Configurable
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.bukkit.api.event.BukkitUserUnVanishEvent
import org.sayandev.sayanvanish.bukkit.api.event.BukkitUserVanishEvent
import org.sayandev.sayanvanish.bukkit.feature.HookFeature
import org.sayandev.stickynote.bukkit.registerListener
import org.sayandev.stickynote.bukkit.warn
import org.sayandev.stickynote.lib.spongepowered.configurate.objectmapping.ConfigSerializable

@RegisteredFeature
@ConfigSerializable
class FeatureHookDiscordSRV(
    @Configurable val sendQuitMessageOnVanish: Boolean = true,
    @Configurable val sendJoinMessageOnUnvanish: Boolean = true,
    @Configurable val quitMessage: String = "%player% left the server",
    @Configurable val joinMessage: String = "%player% joined the server",
): HookFeature("hook_discordsrv", "DiscordSRV") {

    override fun enable() {
        if (hasPlugin()) {
            DiscordSRVImpl(this)
        }
        super.enable()
    }
}

private class DiscordSRVImpl(val feature: FeatureHookDiscordSRV): Listener {

    init {
        registerListener(this)
    }

    @EventHandler
    private fun onUnVanish(event: BukkitUserUnVanishEvent) {
        if (!feature.isActive()) return
        if (!feature.sendJoinMessageOnUnvanish) return
        val user = event.user
        val player = user.player() ?: return
        DiscordSRV.getPlugin().sendJoinMessage(player, feature.joinMessage)
    }

    @EventHandler
    private fun onVanish(event: BukkitUserVanishEvent) {
        if (!feature.isActive()) return
        if (!feature.sendQuitMessageOnVanish) return
        val user = event.user
        val player = user.player() ?: return
        DiscordSRV.getPlugin().sendLeaveMessage(player, feature.quitMessage)
    }

}