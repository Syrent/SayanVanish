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

import org.bukkit.event.EventHandler
import org.sayandev.sayanvanish.api.Permissions
import org.sayandev.sayanvanish.api.feature.Configurable
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.paper.api.event.PaperUserUnVanishEvent
import org.sayandev.sayanvanish.paper.api.event.PaperUserVanishEvent
import org.sayandev.sayanvanish.paper.config.language
import org.sayandev.sayanvanish.paper.feature.ListenedFeature
import kotlinx.serialization.Serializable
import com.charleskorn.kaml.YamlComment
import kotlinx.serialization.SerialName
import kotlinx.serialization.Transient

@RegisteredFeature
@Serializable
@SerialName("fly")
class FeatureFly(
    @YamlComment("Disable fly when player reappears and don't have keep fly permission.")
    @Configurable val disableOnReappear: Boolean = true
) : ListenedFeature() {

    @Transient override val id = "fly"
    override var enabled: Boolean = true

    @EventHandler
    private fun onVanish(event: PaperUserVanishEvent) {
        val user = event.user
        if (!isActive(user)) return
        if (user.hasPermission(Permissions.FLY)) {
            user.player()?.allowFlight = true
            user.player()?.isFlying = true
        }
    }

    @EventHandler
    private fun onUnVanish(event: PaperUserUnVanishEvent) {
        val user = event.user
        if (!isActive(user)) return
        if (!user.hasPermission(Permissions.FLY_KEEP_AFTER_REAPPEAR) && disableOnReappear) {
            user.sendMessageWithPrefix(language.feature.flyDisabled)
            user.player()?.allowFlight = false
            user.player()?.isFlying = false
        }
    }

}