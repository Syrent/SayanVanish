package org.sayandev.sayanvanish.bukkit.feature.features.hook

import net.ess3.api.events.AfkStatusChangeEvent
import net.ess3.api.events.PrivateMessagePreSendEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.sayandev.sayanvanish.api.feature.Configurable
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.cachedVanishUser
import org.sayandev.sayanvanish.bukkit.feature.HookFeature
import org.sayandev.stickynote.bukkit.registerListener
import kotlinx.serialization.Serializable
import com.charleskorn.kaml.YamlComment
import kotlinx.serialization.SerialName
import java.util.*

@RegisteredFeature
@Serializable
@SerialName("hook_essentials")
class FeatureHookEssentials(
    @YamlComment("Prevent Essentials from changing the AFK status of vanished players")
    @Configurable val preventAfkStatusChange: Boolean = true,
    @YamlComment("Prevent players to send private messages to vanished players using Essentials")
    @Configurable val preventPrivateMessage: Boolean = true,
) : HookFeature("hook_essentials", "Essentials") {

    override fun enable() {
        if (hasPlugin()) {
            EssentialsHookImpl(this)
        }
        super.enable()
    }

}

private class EssentialsHookImpl(val feature: FeatureHookEssentials): Listener {

    val lastAfkValue = mutableMapOf<UUID, Boolean>()

    init {
        registerListener(this)
    }

    @EventHandler
    private fun preventAfkStatusChange(event: AfkStatusChangeEvent) {
        if (!feature.preventAfkStatusChange) return
        val user = event.affected.uuid?.cachedVanishUser() ?: return
        if (!feature.isActive(user)) return
        if (!user.isVanished) return
        event.isCancelled = true
    }

    @EventHandler
    private fun preventPrivateMessage(event: PrivateMessagePreSendEvent) {
        if (!feature.preventPrivateMessage) return
        val user = event.recipient.uuid?.cachedVanishUser() ?: return
        if (!feature.isActive(user)) return
        if (user.isVanished) {
            event.sender.sendMessage(com.earth2me.essentials.I18n.tl("errorWithMessage", com.earth2me.essentials.I18n.tl("playerNotFound")))
            event.isCancelled = true
        }
    }
}