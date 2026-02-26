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
package org.sayandev.sayanvanish.paper.api

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.permissions.PermissionDefault
import org.sayandev.sayanvanish.api.Permissions
import org.sayandev.sayanvanish.api.VanishAPI
import org.sayandev.sayanvanish.api.VanishOptions
import org.sayandev.sayanvanish.api.VanishUser
import org.sayandev.sayanvanish.api.feature.Features
import org.sayandev.sayanvanish.paper.api.SayanVanishPaperAPI.Companion.getCachedOrCreateVanishUser
import org.sayandev.sayanvanish.paper.api.event.PaperUserUnVanishEvent
import org.sayandev.sayanvanish.paper.api.event.PaperUserVanishEvent
import org.sayandev.sayanvanish.paper.config.Settings
import org.sayandev.sayanvanish.paper.config.language
import org.sayandev.sayanvanish.paper.feature.features.FeatureLevel
import org.sayandev.sayanvanish.paper.feature.features.hook.FeatureHookLuckPerms
import org.sayandev.stickynote.bukkit.hasPlugin
import org.sayandev.stickynote.bukkit.onlinePlayers
import org.sayandev.stickynote.bukkit.plugin
import org.sayandev.stickynote.bukkit.server
import org.sayandev.stickynote.bukkit.utils.ServerVersion
import java.util.*

open class PaperVanishUser(
    override val uniqueId: UUID,
    override var username: String
) : VanishUser {

    override var serverId = Settings.get().serverId()
    override var currentOptions = VanishOptions.defaultOptions()
    override var isVanished = false
    override var isOnline: Boolean = if (!Settings.get().general.proxyMode) {
        Bukkit.getPlayer(uniqueId) != null
    } else {
        VanishAPI.get().getCacheService().getUsers()[uniqueId]?.isOnline ?: false
    }
    override var vanishLevel: Int = 0
        get() = if (Features.getFeature<FeatureLevel>().levelMethod == FeatureLevel.LevelMethod.PERMISSION) {
            player()?.let { player ->
                player.effectivePermissions
                    .filter { it.permission.startsWith("sayanvanish.level.") }
                    .maxOfOrNull { it.permission.substringAfter("sayanvanish.level.").toIntOrNull() ?: field }
                    ?: if (hasPermission(Permissions.VANISH)) 1 else {
                        if (isVanished) 1 else field
                    }
            } ?: field
        } else {
            field
        }

    fun player(): Player? = Bukkit.getPlayer(uniqueId)
    fun offlinePlayer(): OfflinePlayer = Bukkit.getOfflinePlayer(uniqueId)

    override fun disappear(options: VanishOptions) {
        val vanishEvent = PaperUserVanishEvent(this, options)
        server.pluginManager.callEvent(vanishEvent)
        if (vanishEvent.isCancelled) return
        val options = vanishEvent.options
        currentOptions = options

        if (ServerVersion.supports(9)) {
            player()?.isCollidable = false
        }
        player()?.isSleepingIgnored = true

        player()?.setMetadata("vanished", FixedMetadataValue(plugin, true))

        super.saveDisappear(options)

        // order matters - don't move hideUser before vanish (hideUser have a canSee check for vanish state notify)
        hideForAll()

        sendMessageWithPrefix(language.vanish.vanishStateUpdate, Placeholder.parsed("state", stateText()))
    }

    override fun appear(options: VanishOptions) {
        val unVanishEvent = PaperUserUnVanishEvent(this, options)
        server.pluginManager.callEvent(unVanishEvent)
        if (unVanishEvent.isCancelled) return
        val options = unVanishEvent.options
        currentOptions = options

        if (ServerVersion.supports(9)) {
            player()?.isCollidable = true
        }
        player()?.isSleepingIgnored = false

        player()?.removeMetadata("vanished", plugin)
        showUser()

        super.saveAppear(options)

        sendMessageWithPrefix(language.vanish.vanishStateUpdate, Placeholder.parsed("state", stateText()))
    }

    override fun hasPermission(permission: String): Boolean {
        return if (hasPlugin("LuckPerms")) {
            val luckPermsFeature = Features.getFeature<FeatureHookLuckPerms>()
            /*
            * I have to check if the player is op or not and luckperms feature is enabled so it doesn't disable all feature for op players
            * (bukkit permission check return true for all permissions if the player is op and the permission default is not false)
            * */
            // Can't use luckperms feature isActive per-player, because per-player features check for player permissions and it causes stackoverflow
            if (luckPermsFeature.isActive() && luckPermsFeature.checkPermissionViaLuckPerms) {
                luckPermsFeature.hasPermission(uniqueId, permission)
            } else {
                if (permission.startsWith("sayanvanish.feature.disable.")) {
                    return if (luckPermsFeature.isActive() && luckPermsFeature.checkPermissionViaLuckPermsFeatures) {
                        luckPermsFeature.hasPermission(uniqueId, permission)
                    } else {
                        false
                    }
                }
                player()?.hasPermission(org.bukkit.permissions.Permission(permission, PermissionDefault.FALSE)) == true
            }
        } else {
            if (permission.startsWith("sayanvanish.feature.disable.")) {
                return false
            }
            player()?.hasPermission(org.bukkit.permissions.Permission(permission, PermissionDefault.FALSE)) == true
        }
    }

    fun hideForAll() {
        val player = player()
        if (player != null) {
            for (onlinePlayer in onlinePlayers.filter { it.uniqueId != uniqueId }) {
                val viewer = onlinePlayer.getCachedOrCreateVanishUser()
                if (!viewer.canSee(this)) {
                    hidePlayer(onlinePlayer, player)
                }
            }
        }
        if (currentOptions.notifyStatusChangeToOthers) {
            for (otherUser in VanishAPI.get().getCacheService().getUsers().getOnline().filter { it.username != username && it.generatedVanishUser().canSee(this) }) {
                otherUser.sendMessage(language.vanish.vanishStateOther, Placeholder.parsed("player", username), Placeholder.parsed("state", stateText(true)))
            }
        }
    }

    fun hideFor(target: Player) {
        val player = player() ?: return
        val viewer = target.getCachedOrCreateVanishUser()
        if (!viewer.canSee(this)) {
            hidePlayer(target, player)
        }
    }

    fun showUser() {
        for (onlinePlayer in onlinePlayers.filter { it.uniqueId != this.uniqueId }) {
            showUser(onlinePlayer)
        }
        if (currentOptions.notifyStatusChangeToOthers) {
            for (otherUser in VanishAPI.get().getCacheService().getUsers().getOnline().filter { it.username != this.username && it.generatedVanishUser().canSee(this) }) {
                otherUser.sendMessage(language.vanish.vanishStateOther, Placeholder.parsed("player", username), Placeholder.parsed("state", stateText(false)))
            }
        }
    }

    fun showUser(target: Player) {
        player()?.let { player ->
            showPlayer(target, player)
        }
    }

    private fun showPlayer(player: Player, target: Player) {
        if (ServerVersion.supports(9)) {
            player.showPlayer(plugin, target)
        } else {
            player.showPlayer(target)
        }
    }

    private fun hidePlayer(player: Player, target: Player) {
        if (ServerVersion.supports(9)) {
            player.hidePlayer(plugin, target)
        } else {
            player.hidePlayer(target)
        }
    }

    companion object {
        @JvmStatic
        fun fromUser(vanishUser: VanishUser): PaperVanishUser {
            return PaperVanishUser(vanishUser.uniqueId, vanishUser.username).apply {
                this.isOnline = vanishUser.isOnline
                this.isVanished = vanishUser.isVanished
                this.vanishLevel = vanishUser.vanishLevel
            }
        }

        @JvmSynthetic
        fun VanishUser.bukkitAdapt(): PaperVanishUser {
            return PaperPlatformAdapter.adapt(this)
        }
    }

}
