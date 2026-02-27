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

import github.scarsz.discordsrv.DiscordSRV
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.sayandev.sayanvanish.api.feature.Configurable
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.paper.api.event.PaperUserUnVanishEvent
import org.sayandev.sayanvanish.paper.api.event.PaperUserVanishEvent
import org.sayandev.sayanvanish.paper.feature.HookFeature
import org.sayandev.stickynote.paper.registerListener
import kotlinx.serialization.Serializable
import com.charleskorn.kaml.YamlComment
import kotlinx.serialization.SerialName
import kotlinx.serialization.Transient

@RegisteredFeature
@Serializable
@SerialName("hook_discordsrv")
class FeatureHookDiscordSRV(
    @YamlComment("Send quit message on vanish")
    @Configurable val sendQuitMessageOnVanish: Boolean = true,
    @YamlComment("Send join message on unvanish")
    @Configurable val sendJoinMessageOnUnvanish: Boolean = true,
    @YamlComment("Quit message format")
    @Configurable val quitMessage: String = "%player% left the server",
    @YamlComment("Join message format")
    @Configurable val joinMessage: String = "%player% joined the server",
): HookFeature() {

    @Transient override val id = "hook_discordsrv"
    override var enabled: Boolean = true
    override val plugin: String = "DiscordSRV"

    override fun enable() {
        if (hasPlugin()) {
            DiscordSRVImpl(this)
        }
        super.enable()
    }
}

private class DiscordSRVImpl(val feature: FeatureHookDiscordSRV): Listener {

    init {
        registerListener(this)
    }

    @EventHandler
    private fun onUnVanish(event: PaperUserUnVanishEvent) {
        val user = event.user
        if (!feature.isActive(user)) return
        if (!feature.sendJoinMessageOnUnvanish) return
        val player = user.player() ?: return
        if (event.options.isOnJoin || event.options.isOnQuit) return
        DiscordSRV.getPlugin().sendJoinMessage(player, feature.joinMessage)
    }

    @EventHandler
    private fun onVanish(event: PaperUserVanishEvent) {
        val user = event.user
        if (!feature.isActive(user)) return
        if (!feature.sendQuitMessageOnVanish) return
        val player = user.player() ?: return
        if (event.options.isOnJoin || event.options.isOnQuit) return
        DiscordSRV.getPlugin().sendLeaveMessage(player, feature.quitMessage)
    }

}