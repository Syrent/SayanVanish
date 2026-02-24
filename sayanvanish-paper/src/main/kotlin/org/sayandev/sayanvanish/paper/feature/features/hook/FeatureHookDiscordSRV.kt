package org.sayandev.sayanvanish.paper.feature.features.hook

import github.scarsz.discordsrv.DiscordSRV
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.sayandev.sayanvanish.api.feature.Configurable
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.paper.api.event.PaperUserUnVanishEvent
import org.sayandev.sayanvanish.paper.api.event.PaperUserVanishEvent
import org.sayandev.sayanvanish.paper.feature.HookFeature
import org.sayandev.stickynote.bukkit.registerListener
import kotlinx.serialization.Serializable
import com.charleskorn.kaml.YamlComment
import kotlinx.serialization.SerialName
import kotlinx.serialization.Transient

@RegisteredFeature
@Serializable
@SerialName("hook_discordsrv")
class FeatureHookDiscordSRV(
    @YamlComment("Send quit message on vanish")
    @Configurable val sendQuitMessageOnVanish: Boolean = true,
    @YamlComment("Send join message on unvanish")
    @Configurable val sendJoinMessageOnUnvanish: Boolean = true,
    @YamlComment("Quit message format")
    @Configurable val quitMessage: String = "%player% left the server",
    @YamlComment("Join message format")
    @Configurable val joinMessage: String = "%player% joined the server",
): HookFeature() {

    @Transient override val id = "hook_discordsrv"
    override var enabled: Boolean = true
    override val plugin: String = "DiscordSRV"

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
    private fun onUnVanish(event: PaperUserUnVanishEvent) {
        val user = event.user
        if (!feature.isActive(user)) return
        if (!feature.sendJoinMessageOnUnvanish) return
        val player = user.player() ?: return
        if (event.options.isOnJoin || event.options.isOnQuit) return
        DiscordSRV.getPlugin().sendJoinMessage(player, feature.joinMessage)
    }

    @EventHandler
    private fun onVanish(event: PaperUserVanishEvent) {
        val user = event.user
        if (!feature.isActive(user)) return
        if (!feature.sendQuitMessageOnVanish) return
        val player = user.player() ?: return
        if (event.options.isOnJoin || event.options.isOnQuit) return
        DiscordSRV.getPlugin().sendLeaveMessage(player, feature.quitMessage)
    }

}