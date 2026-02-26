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

import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerGameModeChangeEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.sayandev.sayanvanish.api.VanishAPI
import org.sayandev.sayanvanish.api.feature.Configurable
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.paper.api.PaperVanishUser.Companion.bukkitAdapt
import org.sayandev.sayanvanish.paper.api.SayanVanishPaperAPI.Companion.cachedVanishUser
import org.sayandev.sayanvanish.paper.api.SayanVanishPaperAPI.Companion.getCachedOrCreateVanishUser
import org.sayandev.sayanvanish.paper.api.event.PaperUserUnVanishEvent
import org.sayandev.sayanvanish.paper.api.event.PaperUserVanishEvent
import org.sayandev.sayanvanish.paper.feature.ListenedFeature
import org.sayandev.stickynote.bukkit.*
import org.sayandev.stickynote.bukkit.nms.NMSUtils
import org.sayandev.stickynote.bukkit.nms.NMSUtils.sendPacket
import org.sayandev.stickynote.bukkit.nms.PacketUtils
import org.sayandev.stickynote.bukkit.utils.ServerVersion
import kotlinx.serialization.Serializable
import com.charleskorn.kaml.YamlComment
import kotlinx.serialization.SerialName
import kotlinx.serialization.Transient

@RegisteredFeature
@Serializable
@SerialName("level")
class FeatureLevel(
    @YamlComment(
    "This feature is a CRITICAL feature! do NOT disable this feature if you don't know what you're doing.",
    "",
    "If true, players will see vanished players as spectators. (ONLY WORKS ON MINECRAFT 1.9 AND ABOVE)")
    @Configurable val seeAsSpectator: Boolean = ServerVersion.supports(9),
    @YamlComment(
    "The method to use to determine the vanish level of a player.",
    "",
    "Options:",
    "- PERMISSION: The vanish level is determined by player permission (permission: sayanvanish.level.<level>).",
    "- DATABASE: The vanish level is determined by the vanish level stored in the database. (you can use plugin internal command to change database vanish level)",
    )
    val levelMethod: LevelMethod = LevelMethod.PERMISSION
): ListenedFeature() {

    @Transient override val id = "level"
    override var enabled: Boolean = true
    @Transient override val critical: Boolean = true

    @EventHandler
    private fun onVanish(event: PaperUserVanishEvent) {
        val user = event.user
        if (!isActive(user)) return
        runSync({
            for (onlinePlayer in onlinePlayers.filter { it.uniqueId != user.uniqueId }) {
                val viewer = onlinePlayer.getCachedOrCreateVanishUser()
                if (!viewer.canSee(user)) {
                    user.player()?.let { player ->
                        hidePlayer(onlinePlayer, player)
                        onlinePlayer.sendPacket(PacketUtils.getRemoveEntitiesPacket(player.entityId))
                    }
                } else {
                    if (seeAsSpectator) {
                        user.player()?.let { player -> onlinePlayer.sendPacket(PacketUtils.getUpdateGameModePacket(NMSUtils.getServerPlayer(player), GameMode.SPECTATOR)) }
                    }
                }
            }
        }, 1)
    }

    @EventHandler
    private fun onUnVanish(event: PaperUserUnVanishEvent) {
        val user = event.user
        if (!isActive(user) || !seeAsSpectator) return
        runSync({
            for (onlinePlayer in onlinePlayers.filter { it.uniqueId != user.uniqueId }) {
                val viewer = onlinePlayer.getCachedOrCreateVanishUser()
                if (viewer.canSee(user)) {
                    user.player()?.let { player -> onlinePlayer.sendPacket(PacketUtils.getUpdateGameModePacket(NMSUtils.getServerPlayer(player), player.gameMode)) }
                }
            }
        }, 1)
    }

    @EventHandler
    private fun onGameModeChange(event: PlayerGameModeChangeEvent) {
        val player = event.player
        val user = event.player.cachedVanishUser() ?: return
        if (!isActive(user) || !seeAsSpectator) return
        if (!user.isVanished) return
        for (onlinePlayer in onlinePlayers.filter { it.uniqueId != user.uniqueId }) {
            val viewer = onlinePlayer.getCachedOrCreateVanishUser()
            if (viewer.canSee(user)) {
                runSync({
                    onlinePlayer.sendPacket(PacketUtils.getUpdateGameModePacket(NMSUtils.getServerPlayer(player), GameMode.SPECTATOR))
                }, 5)
            } else {
                hidePlayer(onlinePlayer, player)
                onlinePlayer.sendPacket(PacketUtils.getRemoveEntitiesPacket(player.entityId))
            }
        }
    }

    @EventHandler
    private fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        val vanishUser = player.cachedVanishUser()
        if (((vanishUser != null && !isActive(vanishUser) || !isActive())) || !seeAsSpectator) return
        runSync({
            val viewer = player.getCachedOrCreateVanishUser()
            for (user in VanishAPI.get().getCacheService().getVanishUsers().getVanished().filter { it.bukkitAdapt().player() != null && it.uniqueId != player.uniqueId }) {
                val vanishedPlayer = user.bukkitAdapt().player() ?: continue
                if (viewer.canSee(user)) {
                    player.sendPacket(PacketUtils.getUpdateGameModePacket(NMSUtils.getServerPlayer(vanishedPlayer), GameMode.SPECTATOR))
                }
            }
        }, 1)
    }

    @EventHandler
    private fun hideOthersOnJoin(event: PlayerJoinEvent) {
        val player = event.player
        val user = player.cachedVanishUser()
        if ((user != null && !isActive(user)) || !isActive()) return
        runSync({
            val viewer = player.getCachedOrCreateVanishUser()
            for (user in VanishAPI.get().getCacheService().getVanishUsers().getVanished().filter { it.bukkitAdapt().player() != null && it.uniqueId != player.uniqueId }) {
                val vanishedPlayer = user.bukkitAdapt().player() ?: continue
                if (!viewer.canSee(user)) {
                    hidePlayer(player, vanishedPlayer)
                    player.sendPacket(PacketUtils.getRemoveEntitiesPacket(vanishedPlayer.entityId))
                }
            }
        }, 1)
    }

    private fun hidePlayer(player: Player, target: Player) {
        if (ServerVersion.supports(9)) {
            player.hidePlayer(plugin, target)
        } else {
            player.hidePlayer(target)
        }
    }

    @Serializable
    enum class LevelMethod {
        PERMISSION,
        DATABASE
    }

}
