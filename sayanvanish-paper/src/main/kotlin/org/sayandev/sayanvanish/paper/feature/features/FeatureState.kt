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
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.sayandev.sayanvanish.api.Permissions
import org.sayandev.sayanvanish.api.VanishAPI
import org.sayandev.sayanvanish.api.VanishOptions
import org.sayandev.sayanvanish.api.feature.Configurable
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.paper.api.SayanVanishPaperAPI.Companion.cachedVanishUser
import org.sayandev.sayanvanish.paper.api.SayanVanishPaperAPI.Companion.getCachedOrCreateVanishUser
import org.sayandev.sayanvanish.paper.config.language
import org.sayandev.sayanvanish.paper.feature.ListenedFeature
import org.sayandev.stickynote.paper.launch

@RegisteredFeature
@Serializable
@SerialName("state")
class FeatureState(
    @YamlComment(
    "This is a CRITICAL feature. It is responsible for handling the state of the player when they join or quit the server.",
    "do NOT disable this feature if you don't know what you're doing.",
    "",
    "If true, players will be remembered when they join the server. (if they were vanished before quitting)",
    )
    @Configurable val remember: Boolean = true,
    @YamlComment("Whether to vanish players when they join the server (they also need vanish on join permission)")
    @Configurable val vanishOnJoin: Boolean = false,
    @YamlComment("Whether to reappear players when they quit the server")
    @Configurable val reappearOnQuit: Boolean = false,
    @YamlComment("Whether to check permission when a player joins the server")
    @Configurable val checkPermissionOnQuit: Boolean = true,
    @YamlComment("Whether to check permission when a player quits the server")
    @Configurable val checkPermissionOnJoin: Boolean = true,
) : ListenedFeature() {

    @Transient override val id = "state"
    override var enabled: Boolean = true
    @Transient override val critical: Boolean = true

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        val user = player.cachedVanishUser()
        if ((user != null && !isActive(user)) || !isActive()) return
        val vanishJoinOptions = VanishOptions.Builder().sendMessage(false).notifyStatusChangeToOthers(false).isOnJoin(true).build()

        if (user == null) {
            if (!player.hasPermission(Permissions.VANISH.permission())) {
                return
            }

            val tempUser = player.getCachedOrCreateVanishUser()
            tempUser.isOnline = true

            if (tempUser.hasPermission(Permissions.VANISH_ON_JOIN) || vanishOnJoin) {
                tempUser.isVanished = true
                launch {
                    tempUser.disappear(vanishJoinOptions)
                }
            }

            launch {
                tempUser.saveAndSync()
            }
            return
        }

        user.isOnline = true

        if (checkPermissionOnJoin && !user.hasPermission(Permissions.VANISH)) {
            user.sendMessageWithPrefix(language.vanish.noPermissionToKeepVanished, Placeholder.unparsed("permission", Permissions.VANISH.permission()))
            user.appear(vanishJoinOptions)
            launch {
                user.delete()
            }
            return
        }

        if (user.hasPermission(Permissions.VANISH_ON_JOIN) || (user.isVanished && remember) || vanishOnJoin) {
            user.isVanished = true
            user.disappear(vanishJoinOptions)
        }

        if (user.isVanished) {
            if (user.currentOptions.notifyJoinQuitVanished) {
                for (vanishedUser in VanishAPI.get().getCacheService().getVanishUsers().values.filter { it.canSee(user) }.filter { it.uniqueId != user.uniqueId }) {
                    vanishedUser.sendMessage(language.vanish.joinedTheServerWhileVanished, Placeholder.unparsed("player", user.username))
                }
            }
        }

        launch {
            user.save()
        }

        return
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun updateUserOnQuit(event: PlayerQuitEvent) {
        val player = event.player
        val user = player.cachedVanishUser() ?: return

        if (user.isVanished) {
            if (user.currentOptions.notifyJoinQuitVanished) {
                for (vanishedUser in VanishAPI.get().getCacheService().getVanishUsers().values.filter { it.canSee(user) }.filter { it.uniqueId != user.uniqueId }) {
                    vanishedUser.sendMessage(language.vanish.leftTheServerWhileVanished, Placeholder.unparsed("player", user.username))
                }
            }
        }

        if ((reappearOnQuit && user.isVanished) || (checkPermissionOnQuit && !user.hasPermission(Permissions.VANISH))) {
            user.appear(VanishOptions.Builder().isOnQuit(true).build())
        }
        user.isOnline = false

        launch {
            user.saveAndSync()
        }
    }

}
