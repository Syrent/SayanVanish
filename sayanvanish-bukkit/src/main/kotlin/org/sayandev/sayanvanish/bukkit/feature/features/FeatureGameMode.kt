package org.sayandev.sayanvanish.bukkit.feature.features

import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerGameModeChangeEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.bukkit.feature.ListenedFeature
import org.sayandev.stickynote.bukkit.StickyNote.runSync
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.user
import org.sayandev.sayanvanish.bukkit.api.event.BukkitUserUnVanishEvent
import org.sayandev.sayanvanish.bukkit.api.event.BukkitUserVanishEvent
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment

@RegisteredFeature
@ConfigSerializable
class FeatureGameMode(
    @Comment("The fallback gamemode when the player is not vanished and doesn't have in-memory gamemode.")
    val fallbackMode: GameMode = GameMode.SURVIVAL,
    @Comment("Update gamemode history on gamemode change event.")
    val checkGameModeChange: Boolean = false,
    @Comment("Change gamemode to spectator on double-sneak")
    val checkToggleSneak: Boolean = true,
    val timeWindowTicks: Long = 8
): ListenedFeature("gamemode") {

    @Transient val sneakMap = mutableMapOf<Player, GameMode>()
    @Transient val sneakList = mutableListOf<Player>()

    @EventHandler
    private fun onToggleSneak(event: PlayerToggleSneakEvent) {
        if (!checkToggleSneak) return
        val player = event.player
        val user = player.user() ?: return
        if (!player.isSneaking || !user.isVanished || !isActive(user)) return
        if (sneakList.contains(player)) {
            if (player.gameMode == GameMode.SPECTATOR) {
                val allowFlight = player.allowFlight
                val isFlying = player.isFlying
                player.gameMode = sneakMap[player] ?: fallbackMode
                player.allowFlight = allowFlight
                player.isFlying = isFlying
            } else {
                sneakMap[player] = player.gameMode
                player.gameMode = GameMode.SPECTATOR
            }
        } else {
            if (player.gameMode != GameMode.SPECTATOR) {
                sneakMap[player] = player.gameMode
            }
            sneakList.add(player)
            runSync({
                sneakList.remove(player)
            }, timeWindowTicks)
        }
    }

    @EventHandler
    private fun onGameModeChange(event: PlayerGameModeChangeEvent) {
        if (!checkGameModeChange) return
        val player = event.player
        if (player.user()?.isVanished != false) return
        sneakMap[player] = event.newGameMode
    }

    @EventHandler
    fun onVanish(event: BukkitUserVanishEvent) {
        val user = event.user
        val player = user.player() ?: return
        if (player.gameMode == GameMode.SPECTATOR) return
        val allowFlight = player.allowFlight
        val isFlying = player.isFlying
        sneakMap[player] = player.gameMode
        player.allowFlight = allowFlight
        player.isFlying = isFlying
    }

    @EventHandler
    private fun onUnVanish(event: BukkitUserUnVanishEvent) {
        val user = event.user
        val player = user.player() ?: return
        player.gameMode = sneakMap[player] ?: fallbackMode
        if (player.gameMode == GameMode.SPECTATOR || sneakMap[player] == GameMode.SPECTATOR) return
        val isFlying = player.isFlying
        val allowFlight = player.allowFlight
        player.allowFlight = allowFlight
        player.isFlying = isFlying
    }

}