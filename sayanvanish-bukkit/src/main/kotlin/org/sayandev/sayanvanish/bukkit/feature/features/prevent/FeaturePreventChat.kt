package org.sayandev.sayanvanish.bukkit.feature.features.prevent

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.sayandev.sayanvanish.api.feature.Configurable
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.api.feature.category.FeatureCategories
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.user
import org.sayandev.sayanvanish.bukkit.config.language
import org.sayandev.sayanvanish.bukkit.feature.ListenedFeature
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@RegisteredFeature
@ConfigSerializable
class FeaturePreventChat(
    @Configurable val bypassChar: String = "!"
): ListenedFeature("prevent_chat", category = FeatureCategories.PREVENTION) {

    @EventHandler(priority = EventPriority.HIGHEST)
    @Suppress("DEPRECATION")
    private fun onPlayerChat(event: AsyncPlayerChatEvent) {
        if (event.isCancelled) return
        if (!isActive()) return
        val user = event.player.user() ?: return
        if (!user.isVanished) return
        val message = event.message
        if (message.startsWith(bypassChar)) {
            event.message = message.removePrefix(bypassChar)
        } else {
            user.sendComponent(language.vanish.cantChatWhileVanished, Placeholder.unparsed("char", bypassChar))
            event.isCancelled = true
        }
    }

}