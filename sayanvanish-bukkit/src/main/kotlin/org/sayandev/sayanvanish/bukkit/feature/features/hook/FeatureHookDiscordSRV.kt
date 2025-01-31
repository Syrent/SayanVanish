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
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment

@RegisteredFeature
@ConfigSerializable
data class FeatureHookDiscordSRV(
    @Comment("Send quit message on vanish")
    @Configurable val sendQuitMessageOnVanish: Boolean = true,
    @Comment("Send join message on unvanish")
    @Configurable val sendJoinMessageOnUnvanish: Boolean = true,
    @Comment("Quit message format")
    @Configurable val quitMessage: String = "%player% left the server",
    @Comment("Join message format")
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
        val user = event.user
        if (!feature.isActive(user)) return
        if (!feature.sendJoinMessageOnUnvanish) return
        val player = user.player() ?: return
        if (event.options.isOnJoin || event.options.isOnQuit) return
        DiscordSRV.getPlugin().sendJoinMessage(player, feature.joinMessage)
    }

    @EventHandler
    private fun onVanish(event: BukkitUserVanishEvent) {
        val user = event.user
        if (!feature.isActive(user)) return
        if (!feature.sendQuitMessageOnVanish) return
        val player = user.player() ?: return
        if (event.options.isOnJoin || event.options.isOnQuit) return
        DiscordSRV.getPlugin().sendLeaveMessage(player, feature.quitMessage)
    }

}