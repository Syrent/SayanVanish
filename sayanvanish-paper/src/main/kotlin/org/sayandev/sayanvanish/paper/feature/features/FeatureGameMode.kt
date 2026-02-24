package org.sayandev.sayanvanish.paper.feature.features

import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerGameModeChangeEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.paper.api.SayanVanishBukkitAPI.Companion.cachedVanishUser
import org.sayandev.sayanvanish.paper.feature.ListenedFeature
import org.sayandev.stickynote.bukkit.StickyNote.runSync
import org.sayandev.sayanvanish.paper.api.event.PaperUserUnVanishEvent
import org.sayandev.sayanvanish.paper.api.event.PaperUserVanishEvent
import kotlinx.serialization.Serializable
import com.charleskorn.kaml.YamlComment
import kotlinx.serialization.SerialName
import kotlinx.serialization.Transient

@RegisteredFeature
@Serializable
@SerialName("gamemode")
class FeatureGameMode(
    @YamlComment("The fallback gamemode when the player is not vanished and doesn't have in-memory gamemode.")
    val fallbackMode: GameMode = GameMode.SURVIVAL,
    @YamlComment("Update gamemode history on gamemode change event.")
    val checkGameModeChange: Boolean = false,
    @YamlComment("Change gamemode to spectator on double-sneak")
    val checkToggleSneak: Boolean = true,
    val timeWindowTicks: Long = 8
): ListenedFeature() {

    @Transient override val id = "gamemode"
    override var enabled: Boolean = true

    @Transient val sneakMap = mutableMapOf<Player, GameMode>()
    @Transient val sneakList = mutableListOf<Player>()

    @EventHandler
    private fun onToggleSneak(event: PlayerToggleSneakEvent) {
        if (!checkToggleSneak) return
        val player = event.player
        val user = player.cachedVanishUser() ?: return
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
        if (player.cachedVanishUser()?.isVanished != false) return
        sneakMap[player] = event.newGameMode
    }

    @EventHandler
    fun onVanish(event: PaperUserVanishEvent) {
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
    private fun onUnVanish(event: PaperUserUnVanishEvent) {
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