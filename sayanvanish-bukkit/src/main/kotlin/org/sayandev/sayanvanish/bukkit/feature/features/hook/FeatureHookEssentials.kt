package org.sayandev.sayanvanish.bukkit.feature.features.hook

import net.ess3.api.events.AfkStatusChangeEvent
import net.ess3.api.events.PrivateMessagePreSendEvent
import org.bukkit.event.EventHandler
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.bukkit.feature.HookFeature
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.user
import org.sayandev.stickynote.lib.spongepowered.configurate.objectmapping.ConfigSerializable

@RegisteredFeature
@ConfigSerializable
class FeatureHookEssentials(
    val preventAfkStatusChange: Boolean = true,
    val preventPrivateMessage: Boolean = true
) : HookFeature("hook_essentials", "Essentials") {

    override fun enable() {
        if (hasPlugin()) {
            EssentialsHookImpl(this)
        }
        super.enable()
    }

}

private class EssentialsHookImpl(val feature: FeatureHookEssentials) {

    @EventHandler
    private fun onNPCSpeech(event: AfkStatusChangeEvent) {
        if (!feature.isActive() || feature.preventAfkStatusChange) return
        val user = event.affected.uuid?.user() ?: return
        if (user.isVanished) {
            event.isCancelled = true
        }
    }

    @EventHandler
    private fun onNPCSpeech(event: PrivateMessagePreSendEvent) {
        if (!feature.isActive() || feature.preventPrivateMessage) return
        val user = event.recipient.uuid?.user() ?: return
        if (user.isVanished) {
            event.sender.sendMessage(com.earth2me.essentials.I18n.tl("errorWithMessage", com.earth2me.essentials.I18n.tl("playerNotFound")))
            event.isCancelled = true
        }
    }
}