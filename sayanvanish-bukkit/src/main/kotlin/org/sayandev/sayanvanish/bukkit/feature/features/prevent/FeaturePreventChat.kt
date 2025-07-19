package org.sayandev.sayanvanish.bukkit.feature.features.prevent

import org.sayandev.sayanventure.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Bukkit
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.sayandev.sayanvanish.api.feature.Configurable
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.api.feature.category.FeatureCategories
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.cachedVanishUser
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.user
import org.sayandev.sayanvanish.bukkit.config.language
import org.sayandev.sayanvanish.bukkit.feature.ListenedFeature
import org.sayandev.stickynote.bukkit.StickyNote
import org.sayandev.stickynote.bukkit.plugin
import org.sayandev.stickynote.bukkit.utils.AdventureUtils.component
import kotlinx.serialization.Serializable
import com.charleskorn.kaml.YamlComment
import kotlinx.serialization.SerialName

@RegisteredFeature
@Serializable
@SerialName("prevent_chat")
class FeaturePreventChat(
    @YamlComment("The character that vanished players can use to bypass the chat prevention.")
    @Configurable val bypassChar: String = "!",
    @YamlComment("Requires server restart to apply.")
    val priority: EventPriority = EventPriority.HIGH,
): ListenedFeature("prevent_chat", category = FeatureCategories.PREVENTION) {

    override fun enable() {
        Bukkit.getPluginManager().registerEvent(
            AsyncPlayerChatEvent::class.java,
            this,
            priority,
            { listener: Listener, event: Event ->
                if (event !is AsyncPlayerChatEvent) return@registerEvent
                if (event.isCancelled) return@registerEvent
                val user = event.player.cachedVanishUser() ?: return@registerEvent
                if (!isActive(user)) return@registerEvent
                if (!user.isVanished) return@registerEvent
                val message = event.message
                if (message.startsWith(bypassChar)) {
                    event.message = message.removePrefix(bypassChar)
                } else {
                    user.sendMessage(language.vanish.cantChatWhileVanished.component(Placeholder.unparsed("char", bypassChar)))
                    event.isCancelled = true
                }
            },
            plugin,
            false
        )
        super.enable()
    }

    override fun disable(reload: Boolean) {
        StickyNote.unregisterListener(this)
        super.disable(reload)
    }

}