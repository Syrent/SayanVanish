package org.sayandev.sayanvanish.bukkit.feature.features

import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerGameModeChangeEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.user
import org.sayandev.sayanvanish.bukkit.api.event.BukkitUserUnVanishEvent
import org.sayandev.sayanvanish.bukkit.api.event.BukkitUserVanishEvent
import org.sayandev.sayanvanish.bukkit.feature.ListenedFeature
import org.sayandev.stickynote.bukkit.NMSUtils
import org.sayandev.stickynote.bukkit.PacketUtils
import org.sayandev.stickynote.bukkit.onlinePlayers
import org.sayandev.stickynote.bukkit.plugin
import org.sayandev.stickynote.bukkit.runSync
import org.sayandev.stickynote.lib.spongepowered.configurate.objectmapping.ConfigSerializable

@RegisteredFeature
@ConfigSerializable
class FeatureLevel(
    val seeAsSpectator: Boolean = true
): ListenedFeature("level") {

    @EventHandler
    private fun onVanish(event: BukkitUserVanishEvent) {
        if (!isActive()) return
        runSync({
            val user = event.user
            for (onlinePlayer in onlinePlayers.filter { it.uniqueId != user.uniqueId }) {
                val playerVanishLevel = onlinePlayer.user()?.vanishLevel ?: 0
                if (playerVanishLevel < user.vanishLevel) {
                    user.player()?.let { player ->
                        onlinePlayer.hidePlayer(plugin, player)
                        NMSUtils.sendPacket(onlinePlayer, PacketUtils.getRemoveEntitiesPacket(player.entityId))
                    }
                } else {
                    if (seeAsSpectator) {
                        user.player()?.let { player -> NMSUtils.sendPacket(onlinePlayer, PacketUtils.getUpdateGameModePacket(NMSUtils.getServerPlayer(player), GameMode.SPECTATOR)) }
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
                val playerVanishLevel = onlinePlayer.user()?.vanishLevel ?: 0
                if (playerVanishLevel >= user.vanishLevel) {
                    user.player()?.let { player -> NMSUtils.sendPacket(onlinePlayer, PacketUtils.getUpdateGameModePacket(NMSUtils.getServerPlayer(player), player.gameMode)) }
                }
            }
        }, 1)
    }

    @EventHandler
    private fun onGameModeChange(event: PlayerGameModeChangeEvent) {
        if (!isActive() || seeAsSpectator) return
        val player = event.player
        val user = event.player.user() ?: return
        for (onlinePlayer in onlinePlayers.filter { it.uniqueId != user.uniqueId }) {
            val playerVanishLevel = onlinePlayer.user()?.vanishLevel ?: 0
            if (playerVanishLevel >= user.vanishLevel) {
                NMSUtils.sendPacket(onlinePlayer, PacketUtils.getUpdateGameModePacket(NMSUtils.getServerPlayer(player), player.gameMode))
            } else {
                NMSUtils.sendPacket(onlinePlayer, PacketUtils.getUpdateGameModePacket(NMSUtils.getServerPlayer(player), GameMode.SPECTATOR))
            }
        }
    }

    @EventHandler
    private fun onPlayerJoin(event: PlayerJoinEvent) {
        if (!isActive() || seeAsSpectator) return
        runSync({
            val player = event.player
            val playerVanishLevel = player.user()?.vanishLevel ?: 0
            for (user in SayanVanishBukkitAPI.getInstance().getVanishedUsers().filter { it.player() != null && it.uniqueId != player.uniqueId }) {
                if (playerVanishLevel >= user.vanishLevel) {
                    NMSUtils.sendPacket(player, PacketUtils.getUpdateGameModePacket(NMSUtils.getServerPlayer(player), GameMode.SPECTATOR))
                }
            }
        }, 1)
    }

}