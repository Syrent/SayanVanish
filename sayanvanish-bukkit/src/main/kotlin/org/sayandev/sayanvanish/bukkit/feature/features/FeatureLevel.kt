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
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment

@RegisteredFeature
@ConfigSerializable
data class FeatureLevel(
    override var enabled: Boolean = true,
    @Comment("""
    This feature is a CRITICAL feature! do NOT disable this feature if you don't know what you're doing.
    
    If true, players will see vanished players as spectators. (ONLY WORKS ON MINECRAFT 1.9 AND ABOVE)""")
    @Configurable val seeAsSpectator: Boolean = ServerVersion.supports(9),
    @Comment("""
    The method to use to determine the vanish level of a player.
    
    Options:
    - PERMISSION: The vanish level is determined by player permission (permission: sayanvanish.level.<level>).
    - DATABASE: The vanish level is determined by the vanish level stored in the database. (you can use plugin internal command to change database vanish level)
    """)
    val levelMethod: LevelMethod = LevelMethod.PERMISSION
): ListenedFeature("level", critical = true) {

    @EventHandler
    private fun onVanish(event: BukkitUserVanishEvent) {
        val user = event.user
        if (!isActive(user)) return
        runSync({
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
        val user = event.user
        if (!isActive(user) || !seeAsSpectator) return
        runSync({
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
        val player = event.player
        val user = event.player.user() ?: return
        if (!isActive(user) || !seeAsSpectator) return
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
        val player = event.player
        val user = player.user()
        if (((user != null && !isActive(user) || !isActive())) || !seeAsSpectator) return
        runSync({
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
        val player = event.player
        val user = player.user()
        if ((user != null && !isActive(user)) || !isActive()) return
        runSync({
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