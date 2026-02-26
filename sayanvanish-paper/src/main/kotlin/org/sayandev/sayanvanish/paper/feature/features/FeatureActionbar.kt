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
package org.sayandev.sayanvanish.paper.feature.features

import com.charleskorn.kaml.YamlComment
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.bukkit.event.EventHandler
import org.sayandev.sayanvanish.api.feature.Configurable
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.paper.api.SayanVanishPaperAPI.Companion.cachedVanishUser
import org.sayandev.sayanvanish.paper.api.event.PaperUserUnVanishEvent
import org.sayandev.sayanvanish.paper.api.event.PaperUserVanishEvent
import org.sayandev.sayanvanish.paper.feature.ListenedFeature
import org.sayandev.stickynote.bukkit.StickyNote
import org.sayandev.stickynote.bukkit.launch
import org.sayandev.stickynote.bukkit.onlinePlayers

@RegisteredFeature
@Serializable
@SerialName("actionbar")
class FeatureActionbar(
    @YamlComment("The content of the actionbar message.")
    @Configurable val content: String = "<gray>You are currently vanished!",
    @YamlComment("The delay before the actionbar message is sent. doesn't really matter.")
    @Configurable val delayMillis: Long = 1000,
    @YamlComment("The period between each actionbar message. values higher than 2000 will make it not always visible.")
    @Configurable val periodMillis: Long = 1000,
) : ListenedFeature() {

    @Transient override val id = "actionbar"
    override var enabled: Boolean = true

    @EventHandler
    private fun onVanish(event: PaperUserVanishEvent) {
        val user = event.user
        if (!isActive(user)) return
        user.sendActionbar(content)
    }

    @EventHandler
    private fun onUnVanish(event: PaperUserUnVanishEvent) {
        val user = event.user
        if (!isActive(user)) return
        user.sendActionbar("")
    }

    override fun enable() {
        launch {
            delay(delayMillis)
            while (StickyNote.plugin().isEnabled && enabled && isActive) {
                // TODO: remove this warning
//                warn("vanished: ${VanishAPI.get().getCacheService().getVanishUsers().values.joinToString(" ,") { "${it.username}:${it.isVanished}" }}")
                for (user in onlinePlayers.mapNotNull { it.cachedVanishUser() }.filter { it.isVanished }) {
                    if (!isActive(user)) continue
                    user.sendActionbar(content)
                }
                delay(periodMillis)
            }
        }
        super.enable()
    }

}