package org.sayandev.sayanvanish.bukkit.feature.features.prevent

import org.bukkit.event.EventHandler
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.api.feature.category.FeatureCategories
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.user
import org.sayandev.sayanvanish.bukkit.config.language
import org.sayandev.sayanvanish.bukkit.feature.ListenedFeature
import org.sayandev.stickynote.bukkit.utils.AdventureUtils.component
import org.sayandev.stickynote.lib.spongepowered.configurate.objectmapping.ConfigSerializable

@RegisteredFeature
@ConfigSerializable
class FeaturePreventChat: ListenedFeature("prevent_chat", category = FeatureCategories.PREVENTION) {

    @EventHandler
    private fun onPlayerChat(event: AsyncPlayerChatEvent) {
        if (!isActive()) return
        val user = event.player.user() ?: return
        if (!user.isVanished) return
        val message = event.message
        if (message.startsWith("!")) {
            event.message = message.removePrefix("!")
        } else {
            user.sendMessage(language.vanish.cantChatWhileVanished.component())
            event.isCancelled = true
        }
    }

}