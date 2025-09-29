package org.sayandev.sayanvanish.bungeecord.feature.features.prevent

import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.event.ChatEvent
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority
import org.sayandev.sayanvanish.api.feature.Configurable
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.api.feature.category.FeatureCategories
import org.sayandev.sayanvanish.bungeecord.api.SayanVanishBungeeAPI.Companion.user
import org.sayandev.sayanvanish.bungeecord.feature.ListenedFeature
import org.sayandev.sayanvanish.proxy.config.language
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment

@RegisteredFeature
@ConfigSerializable
class FeaturePreventChat(
    @Comment("The character that vanished players can use to bypass the chat prevention.")
    @Configurable val bypassChar: String = "!"
) : ListenedFeature("prevent_chat", category = FeatureCategories.PREVENTION) {

    @EventHandler(priority = EventPriority.HIGH)
    fun onPlayerChat(event: ChatEvent) {
        val player = event.sender as? ProxiedPlayer ?: return
        val user = player.user() ?: return
        if (!isActive(user)) return
        if (!user.isVanished) return

        val message = event.message
        if (message.startsWith(bypassChar)) {
            event.message = message.removePrefix(bypassChar)
        } else {
            user.sendComponent(language.vanish.cantChatWhileVanished)
            event.isCancelled = true
        }
    }
}