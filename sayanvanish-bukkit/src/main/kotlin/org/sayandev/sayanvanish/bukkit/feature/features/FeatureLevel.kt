package org.sayandev.sayanvanish.bukkit.feature.features

import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerGameModeChangeEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.sayandev.sayanvanish.api.Permission
import org.sayandev.sayanvanish.api.feature.Configurable
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.user
import org.sayandev.sayanvanish.bukkit.api.event.BukkitUserUnVanishEvent
import org.sayandev.sayanvanish.bukkit.api.event.BukkitUserVanishEvent
import org.sayandev.sayanvanish.bukkit.feature.ListenedFeature
import org.sayandev.stickynote.bukkit.*
import org.sayandev.stickynote.bukkit.nms.NMSUtils
import org.sayandev.stickynote.bukkit.nms.NMSUtils.sendPacket
import org.sayandev.stickynote.bukkit.nms.PacketUtils
import org.sayandev.stickynote.bukkit.utils.ServerVersion
import org.sayandev.stickynote.lib.spongepowered.configurate.objectmapping.ConfigSerializable

@RegisteredFeature
@ConfigSerializable
class FeatureLevel(
    @Configurable val seeAsSpectator: Boolean = ServerVersion.supports(9),
    val levelMethod: LevelMethod = LevelMethod.PERMISSION
): ListenedFeature("level") {

    @EventHandler
    private fun onVanish(event: BukkitUserVanishEvent) {
        if (!isActive()) return
        runSync({
            val user = event.user
            for (onlinePlayer in onlinePlayers.filter { it.uniqueId != user.uniqueId }) {
                val playerVanishLevel = onlinePlayer.user()?.vanishLevel ?: -1
                if (playerVanishLevel < user.vanishLevel || !onlinePlayer.hasPermission(Permission.VANISH.permission())) {
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
    private fun onUnVanish(event: BukkitUserUnVanishEvent) {
        if (!isActive() || !seeAsSpectator) return
        runSync({
            val user = event.user
            for (onlinePlayer in onlinePlayers.filter { it.uniqueId != user.uniqueId }) {
                val playerVanishLevel = onlinePlayer.user()?.vanishLevel ?: -1
                if (playerVanishLevel >= user.vanishLevel) {
                    user.player()?.let { player -> onlinePlayer.sendPacket(PacketUtils.getUpdateGameModePacket(NMSUtils.getServerPlayer(player), player.gameMode)) }
                }
            }
        }, 1)
    }

    @EventHandler
    private fun onGameModeChange(event: PlayerGameModeChangeEvent) {
        if (!isActive() || !seeAsSpectator) return
        val player = event.player
        val user = event.player.user() ?: return
        if (!user.isVanished) return
        for (onlinePlayer in onlinePlayers.filter { it.uniqueId != user.uniqueId }) {
            val playerVanishLevel = onlinePlayer.user()?.vanishLevel ?: -1
            if (playerVanishLevel >= user.vanishLevel) {
                onlinePlayer.sendPacket(PacketUtils.getUpdateGameModePacket(NMSUtils.getServerPlayer(player), GameMode.SPECTATOR))
            } else {
                hidePlayer(onlinePlayer, player)
                onlinePlayer.sendPacket(PacketUtils.getRemoveEntitiesPacket(player.entityId))
            }
        }
    }

    @EventHandler
    private fun onPlayerJoin(event: PlayerJoinEvent) {
        if (!isActive() || !seeAsSpectator) return
        runSync({
            val player = event.player
            val playerVanishLevel = player.user()?.vanishLevel ?: -1
            for (user in SayanVanishBukkitAPI.getInstance().getVanishedUsers().filter { it.player() != null && it.uniqueId != player.uniqueId }) {
                val vanishedPlayer = user.player() ?: continue
                if (playerVanishLevel >= user.vanishLevel && user.hasPermission(Permission.VANISH.permission())) {
                    player.sendPacket(PacketUtils.getUpdateGameModePacket(NMSUtils.getServerPlayer(vanishedPlayer), GameMode.SPECTATOR))
                }
            }
        }, 1)
    }

    @EventHandler
    private fun hideOthersOnJoin(event: PlayerJoinEvent) {
        if (!isActive()) return
        runSync({
            val player = event.player
            val playerVanishLevel = player.user()?.vanishLevel ?: -1
            for (user in SayanVanishBukkitAPI.getInstance().getVanishedUsers().filter { it.player() != null && it.uniqueId != player.uniqueId }) {
                val vanishedPlayer = user.player() ?: continue
                if (playerVanishLevel < user.vanishLevel || !player.hasPermission(Permission.VANISH.permission())) {
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

    enum class LevelMethod {
        PERMISSION,
        DATABASE
    }

}