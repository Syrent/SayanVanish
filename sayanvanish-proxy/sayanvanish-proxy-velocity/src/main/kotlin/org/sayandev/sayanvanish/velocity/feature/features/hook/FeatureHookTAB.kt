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
package org.sayandev.sayanvanish.velocity.feature.features.hook

import me.neznamy.tab.api.TabPlayer
import me.neznamy.tab.api.integration.VanishIntegration
import org.sayandev.sayanvanish.api.VanishAPI
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.velocity.api.SayanVanishVelocityAPI
import org.sayandev.sayanvanish.velocity.api.VelocityVanishUser
import org.sayandev.sayanvanish.velocity.feature.HookFeature
import org.sayandev.stickynote.velocity.plugin
import kotlinx.serialization.Serializable
import com.charleskorn.kaml.YamlComment
import kotlinx.serialization.SerialName
import kotlinx.serialization.Transient
import org.sayandev.sayanvanish.api.Platform


@RegisteredFeature
@Serializable
@SerialName("hook_tab")
class FeatureHookTAB : HookFeature() {

    @Transient override val id = "hook_tab"
    override var enabled: Boolean = true
    override val plugin: String = "tab"

    override fun enable() {
        if (hasPlugin()) {
            VanishIntegrationTAB(this).register()
        }
        super.enable()
    }

    override fun disable(reload: Boolean) {
        if (hasPlugin()) {
            VanishIntegrationTAB(this).unregister()
        }
        super.disable(reload)
    }
}

private class VanishIntegrationTAB(val feature: FeatureHookTAB): VanishIntegration(plugin.container.description.name.get()) {
    override fun isVanished(player: TabPlayer): Boolean {
        return VanishAPI.get().getCacheService().getVanishUsers()[player.uniqueId]?.isVanished == true
    }

    override fun canSee(viewer: TabPlayer, target: TabPlayer): Boolean {
        if (viewer.uniqueId == target.uniqueId) return true
        val targetUser = VanishAPI.get().getCacheService().getVanishUsers()[target.uniqueId] ?: return true
        val viewerUser = VanishAPI.get().getCacheService().getVanishUsers()[viewer.uniqueId] ?: VelocityVanishUser(viewer.uniqueId, viewer.name)
        return VanishAPI.get().canSee(viewerUser, targetUser)
    }
}