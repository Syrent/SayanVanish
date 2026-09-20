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
package org.sayandev.sayanvanish.velocity.feature.features.prevent

import com.charleskorn.kaml.YamlComment
import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.PlayerChatEvent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.sayandev.sayanvanish.api.feature.Configurable
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.api.feature.category.FeatureCategories
import org.sayandev.sayanvanish.proxy.config.language
import org.sayandev.sayanvanish.velocity.api.SayanVanishVelocityAPI.Companion.getCachedOrCreateVanishUser
import org.sayandev.sayanvanish.velocity.api.SayanVanishVelocityAPI.Companion.getOrCreateUser
import org.sayandev.sayanvanish.velocity.feature.ListenedFeature

@RegisteredFeature
@Serializable
@SerialName("prevent_chat")
class FeaturePreventChat(
    @YamlComment("The character that vanished players can use to bypass the chat prevention.")
    @Configurable val bypassChar: String = "!"
) : ListenedFeature() {

    @Transient override val id = "prevent_chat"
    override val category = FeatureCategories.PREVENTION
    override var enabled: Boolean = false

    @Subscribe(order = PostOrder.LAST)
    fun onPlayerChat(event: PlayerChatEvent) {
        val player = event.player
        val user = player.getCachedOrCreateVanishUser()
        if (!isActive(user)) return
        if (!user.isVanished) return

        val message = event.message
        if (message.startsWith(bypassChar)) {
            event.result = PlayerChatEvent.ChatResult.message(message.removePrefix(bypassChar))
        } else {
            user.sendMessage(language.vanish.cantChatWhileVanished, Placeholder.unparsed("char", bypassChar))
            event.result = PlayerChatEvent.ChatResult.denied()
        }
    }
}