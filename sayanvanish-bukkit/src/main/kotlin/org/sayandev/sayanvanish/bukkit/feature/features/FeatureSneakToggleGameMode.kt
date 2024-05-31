package org.sayandev.sayanvanish.bukkit.feature.features

import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.bukkit.feature.ListenedFeature
import org.sayandev.stickynote.bukkit.StickyNote.runSync
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.user
import org.sayandev.stickynote.lib.spongepowered.configurate.objectmapping.ConfigSerializable

@RegisteredFeature
@ConfigSerializable
class FeatureSneakToggleGameMode: ListenedFeature("sneak_toggle_gamemode") {

    @Transient val sneakMap = mutableMapOf<Player, GameMode>()
    @Transient val sneakList = mutableListOf<Player>()

    @EventHandler
    private fun onToggleSneak(event: PlayerToggleSneakEvent) {
        val player = event.player
        if (!player.isSneaking || player.user()?.isVanished != true || !isActive()) return
        if (sneakList.contains(player)) {
            if (player.gameMode == GameMode.SPECTATOR) {
                player.gameMode = sneakMap[player]!!
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
            }, 8)
        }
    }

}