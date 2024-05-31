package org.sayandev.sayanvanish.bukkit.feature.features

import org.bukkit.event.EventHandler
import org.sayandev.sayanvanish.api.Permission
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.bukkit.api.event.BukkitUserUnVanishEvent
import org.sayandev.sayanvanish.bukkit.api.event.BukkitUserVanishEvent
import org.sayandev.sayanvanish.bukkit.feature.ListenedFeature
import org.sayandev.stickynote.lib.spongepowered.configurate.objectmapping.ConfigSerializable

@RegisteredFeature
@ConfigSerializable
class FeatureFly(
    val disableOnReappear: Boolean = true
) : ListenedFeature("fly") {

    @EventHandler
    private fun onVanish(event: BukkitUserVanishEvent) {
        if (!isActive()) return
        val user = event.user
        if (user.hasPermission(Permission.FLY)) {
            user.player()?.allowFlight = true
            user.player()?.isFlying = true
        }
    }

    @EventHandler
    private fun onUnVanish(event: BukkitUserUnVanishEvent) {
        if (!isActive()) return
        val user = event.user
        if (!user.hasPermission(Permission.FLY) && disableOnReappear) {
            user.player()?.allowFlight = false
            user.player()?.isFlying = false
        }
    }

}