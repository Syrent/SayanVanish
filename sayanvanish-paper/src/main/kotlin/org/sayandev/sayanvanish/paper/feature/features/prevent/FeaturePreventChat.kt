/*
 * This file is part of SayanVanish, licensed under the GNU General Public License v3.0.
 *
 * Copyright (c) 2026 Sayan Development and contributors
 *
 * SayanVanish is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SayanVanish is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.sayandev.sayanvanish.paper.feature.features.prevent

import com.charleskorn.kaml.YamlComment
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Bukkit
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.sayandev.sayanvanish.api.feature.Configurable
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.api.feature.category.FeatureCategories
import org.sayandev.sayanvanish.paper.api.SayanVanishPaperAPI.Companion.cachedVanishUser
import org.sayandev.sayanvanish.paper.config.language
import org.sayandev.sayanvanish.paper.feature.ListenedFeature
import org.sayandev.stickynote.paper.StickyNote
import org.sayandev.stickynote.paper.plugin

@RegisteredFeature
@Serializable
@SerialName("prevent_chat")
class FeaturePreventChat(
    @YamlComment("The character that vanished players can use to bypass the chat prevention.")
    @Configurable val bypassChar: String = "!",
    @YamlComment("Requires server restart to apply.")
    val priority: EventPriority = EventPriority.HIGH,
): ListenedFeature() {

    @Transient override val id = "prevent_chat"
    override var enabled: Boolean = true
    @Transient override val category: FeatureCategories = FeatureCategories.PREVENTION

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
                    user.sendMessage(language.vanish.cantChatWhileVanished, Placeholder.unparsed("char", bypassChar))
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