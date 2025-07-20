package org.sayandev.sayanvanish.bukkit.feature.features.hook

import net.citizensnpcs.api.ai.speech.SpeechContext
import net.citizensnpcs.api.ai.speech.event.NPCSpeechEvent
import net.citizensnpcs.api.ai.speech.event.SpeechEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.sayandev.sayanvanish.api.VanishAPI
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.bukkit.api.BukkitVanishUser.Companion.bukkitAdapt
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.cachedVanishUser
import org.sayandev.sayanvanish.bukkit.feature.HookFeature
import org.sayandev.stickynote.bukkit.registerListener
import kotlinx.serialization.Serializable
import com.charleskorn.kaml.YamlComment
import kotlinx.serialization.SerialName

@RegisteredFeature
@Serializable
@SerialName("hook_citizens")
class FeatureHookCitizens(
    override var enabled: Boolean = true,
    @YamlComment("Will cancel npc speech event if context of speech contains a vanished player")
    val checkSpeech: Boolean = true,
): HookFeature("hook_citizens", "Citizens", enabled) {

    override fun enable() {
        if (hasPlugin()) {
            CitizensHookImpl(this)
        }
        super.enable()
    }

}

private class CitizensHookImpl(val feature: FeatureHookCitizens): Listener {

    init {
        registerListener(this)
    }

    @EventHandler
    private fun onNPCSpeech(event: NPCSpeechEvent) {
        if (!feature.checkSpeech) return
        if (!feature.isActive()) return
        val hasContext = checkContext(event.context)
        if (!hasContext) event.isCancelled = true
    }

    @EventHandler
    private fun onSpeech(event: SpeechEvent) {
        if (!feature.checkSpeech) return
        if (!feature.isActive()) return
        val hasContext = checkContext(event.context)
        if (!hasContext) event.isCancelled = true
    }

    private fun checkContext(context: SpeechContext): Boolean {
        val contains = VanishAPI.get().getCacheService().getVanishUsers().getVanished().filter { it.isOnline }.mapNotNull { it.bukkitAdapt().player() }.any { context.message.contains(it.name) }
        if (contains) return false

        val iterator = context.iterator()
        while (iterator.hasNext()) {
            val recipient = iterator.next()
            val player = recipient.getEntity() as? Player ?: continue
            val user = player.cachedVanishUser() ?: continue
            if (user.isVanished) {
                iterator.remove();
            }
        }

        return context.hasRecipients();
    }
}