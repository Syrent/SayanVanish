package org.sayandev.sayanvanish.bukkit.feature.features

import org.bukkit.GameMode
import org.bukkit.block.Container
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.util.Vector
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.user
import org.sayandev.sayanvanish.bukkit.feature.ListenedFeature
import org.sayandev.stickynote.bukkit.NMSUtils
import org.sayandev.stickynote.bukkit.PacketUtils
import org.sayandev.stickynote.bukkit.runSync
import org.sayandev.stickynote.bukkit.warn
import org.sayandev.stickynote.lib.spongepowered.configurate.objectmapping.ConfigSerializable

@RegisteredFeature
@ConfigSerializable
class FeatureSilentContainer: ListenedFeature("silent_container") {

    @EventHandler(ignoreCancelled = true)
    private fun onPlayerInteract(event: PlayerInteractEvent) {
        if (!isActive()) return
        val player = event.player
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        val clickedBlock = event.clickedBlock ?: return
        if (clickedBlock.state !is Container) return
        val user = player.user() ?: return
        if (!user.isVanished) return
        if (player.gameMode == GameMode.SPECTATOR) return

        val gamemode = player.gameMode
        val flight = player.allowFlight
        val fly = player.isFlying
        val velocity = player.velocity

        player.allowFlight = true
        player.isFlying = true
        player.gameMode = GameMode.SPECTATOR
        player.velocity = Vector(0.0, 0.0, 0.0)

        runSync({
            player.gameMode = gamemode
            player.allowFlight = flight
            player.isFlying = fly
            player.velocity = velocity
        }, 2)
    }

}