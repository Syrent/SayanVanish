package org.sayandev.sayanvanish.bukkit.feature.features.hook

import net.ess3.api.events.AfkStatusChangeEvent
import net.ess3.api.events.PrivateMessagePreSendEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.sayandev.sayanvanish.api.SayanVanishAPI.Companion.user
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.bukkit.feature.HookFeature
import org.sayandev.stickynote.bukkit.registerListener
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment

@RegisteredFeature
@ConfigSerializable
data class FeatureHookEssentials(
    @Comment("Prevent Essentials from changing the AFK status of vanished players")
    val preventAfkStatusChange: Boolean = true,
    @Comment("Prevent players to send private messages to vanished players using Essentials")
    val preventPrivateMessage: Boolean = true
) : HookFeature("hook_essentials", "Essentials") {

    override fun enable() {
        if (hasPlugin()) {
            EssentialsHookImpl(this)
        }
        super.enable()
    }

}

private class EssentialsHookImpl(val feature: FeatureHookEssentials): Listener {

    init {
        registerListener(this)
    }

    @EventHandler
    private fun onNPCSpeech(event: AfkStatusChangeEvent) {
        val user = event.affected.uuid?.user() ?: return
        if (!feature.isActive(user) || feature.preventAfkStatusChange) return
        if (user.isVanished) {
            event.isCancelled = true
        }
    }

    @EventHandler
    private fun onNPCSpeech(event: PrivateMessagePreSendEvent) {
        val user = event.recipient.uuid?.user() ?: return
        if (!feature.isActive(user) || feature.preventPrivateMessage) return
        if (user.isVanished) {
            event.sender.sendMessage(com.earth2me.essentials.I18n.tl("errorWithMessage", com.earth2me.essentials.I18n.tl("playerNotFound")))
            event.isCancelled = true
        }
    }
}