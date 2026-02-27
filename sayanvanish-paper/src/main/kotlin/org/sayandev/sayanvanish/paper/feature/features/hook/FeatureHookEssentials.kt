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
package org.sayandev.sayanvanish.paper.feature.features.hook

import net.ess3.api.events.AfkStatusChangeEvent
import net.ess3.api.events.PrivateMessagePreSendEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.sayandev.sayanvanish.api.feature.Configurable
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.paper.api.SayanVanishPaperAPI.Companion.cachedVanishUser
import org.sayandev.sayanvanish.paper.feature.HookFeature
import org.sayandev.stickynote.paper.registerListener
import kotlinx.serialization.Serializable
import com.charleskorn.kaml.YamlComment
import kotlinx.serialization.SerialName
import kotlinx.serialization.Transient
import java.util.*

@RegisteredFeature
@Serializable
@SerialName("hook_essentials")
class FeatureHookEssentials(
    @YamlComment("Prevent Essentials from changing the AFK status of vanished players")
    @Configurable val preventAfkStatusChange: Boolean = true,
    @YamlComment("Prevent players to send private messages to vanished players using Essentials")
    @Configurable val preventPrivateMessage: Boolean = true,
) : HookFeature() {

    @Transient override val id = "hook_essentials"
    override var enabled: Boolean = true
    override val plugin: String = "Essentials"

    override fun enable() {
        if (hasPlugin()) {
            EssentialsHookImpl(this)
        }
        super.enable()
    }

}

private class EssentialsHookImpl(val feature: FeatureHookEssentials): Listener {

    val lastAfkValue = mutableMapOf<UUID, Boolean>()

    init {
        registerListener(this)
    }

    @EventHandler
    private fun preventAfkStatusChange(event: AfkStatusChangeEvent) {
        if (!feature.preventAfkStatusChange) return
        val user = event.affected.uuid?.cachedVanishUser() ?: return
        if (!feature.isActive(user)) return
        if (!user.isVanished) return
        event.isCancelled = true
    }

    @EventHandler
    private fun preventPrivateMessage(event: PrivateMessagePreSendEvent) {
        if (!feature.preventPrivateMessage) return
        val user = event.recipient.uuid?.cachedVanishUser() ?: return
        if (!feature.isActive(user)) return
        if (user.isVanished) {
            event.sender.sendMessage(com.earth2me.essentials.I18n.tl("errorWithMessage", com.earth2me.essentials.I18n.tl("playerNotFound")))
            event.isCancelled = true
        }
    }
}