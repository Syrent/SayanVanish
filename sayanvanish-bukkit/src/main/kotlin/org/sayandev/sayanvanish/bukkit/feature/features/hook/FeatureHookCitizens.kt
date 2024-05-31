package org.sayandev.sayanvanish.bukkit.feature.features.hook

import net.citizensnpcs.api.ai.speech.SpeechContext
import net.citizensnpcs.api.ai.speech.event.NPCSpeechEvent
import net.citizensnpcs.api.ai.speech.event.SpeechEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI
import org.sayandev.sayanvanish.bukkit.feature.HookFeature
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.user
import org.sayandev.stickynote.lib.spongepowered.configurate.objectmapping.ConfigSerializable

@RegisteredFeature
@ConfigSerializable
class FeatureHookCitizens: HookFeature("hook_citizens", "Citizens") {

    override fun enable() {
        if (hasPlugin()) {
            CitizensHookImpl(this)
        }
        super.enable()
    }

}

private class CitizensHookImpl(val feature: FeatureHookCitizens) {

    @EventHandler
    private fun onNPCSpeech(event: NPCSpeechEvent) {
        if (!feature.isActive()) return
        val hasContext = checkContext(event.context)
        if (!hasContext) event.isCancelled = true
    }

    @EventHandler
    private fun onSpeech(event: SpeechEvent) {
        if (!feature.isActive()) return
        val hasContext = checkContext(event.context)
        if (!hasContext) event.isCancelled = true
    }

    private fun checkContext(context: SpeechContext): Boolean {
        val contains = SayanVanishBukkitAPI.getInstance().getVanishedUsers().filter { it.isOnline }.mapNotNull { it.player() }.any { context.message.contains(it.name) }
        if (contains) return false

        val iterator = context.iterator()
        while (iterator.hasNext()) {
            val recipient = iterator.next()
            val player = recipient.getEntity() as? Player ?: continue
            val user = player.user() ?: continue
            if (user.isVanished) {
                iterator.remove();
            }
        }

        return context.hasRecipients();
    }
}