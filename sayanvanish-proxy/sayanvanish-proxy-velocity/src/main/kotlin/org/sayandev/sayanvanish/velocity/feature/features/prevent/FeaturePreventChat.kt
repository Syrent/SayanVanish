package org.sayandev.sayanvanish.velocity.feature.features.prevent

import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.PlayerChatEvent
import org.sayandev.sayanvanish.api.feature.Configurable
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.api.feature.category.FeatureCategories
import org.sayandev.sayanvanish.proxy.config.language
import org.sayandev.sayanvanish.velocity.api.SayanVanishVelocityAPI.Companion.getOrCreateUser
import org.sayandev.sayanvanish.velocity.feature.ListenedFeature
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment

@RegisteredFeature
@ConfigSerializable
class FeaturePreventChat(
    @Comment("The character that vanished players can use to bypass the chat prevention.")
    @Configurable val bypassChar: String = "!"
) : ListenedFeature("prevent_chat", category = FeatureCategories.PREVENTION) {

    @Subscribe(order = PostOrder.LAST)
    fun onPlayerChat(event: PlayerChatEvent) {
        val player = event.player
        val user = player.getOrCreateUser()
        if (!isActive(user)) return
        if (!user.isVanished) return

        val message = event.message
        if (message.startsWith(bypassChar)) {
            event.result = PlayerChatEvent.ChatResult.message(message.removePrefix(bypassChar))
        } else {
            user.sendComponent(language.vanish.cantChatWhileVanished)
            event.result = PlayerChatEvent.ChatResult.denied()
        }
    }
}