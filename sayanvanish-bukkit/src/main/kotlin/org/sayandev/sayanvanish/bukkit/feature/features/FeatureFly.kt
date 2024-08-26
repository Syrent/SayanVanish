package org.sayandev.sayanvanish.bukkit.feature.features

import org.bukkit.event.EventHandler
import org.sayandev.sayanvanish.api.Permission
import org.sayandev.sayanvanish.api.feature.Configurable
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.bukkit.api.event.BukkitUserUnVanishEvent
import org.sayandev.sayanvanish.bukkit.api.event.BukkitUserVanishEvent
import org.sayandev.sayanvanish.bukkit.config.language
import org.sayandev.sayanvanish.bukkit.feature.ListenedFeature
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@RegisteredFeature
@ConfigSerializable
class FeatureFly(
    @Configurable val disableOnReappear: Boolean = true
) : ListenedFeature("fly") {

    @EventHandler
    private fun onVanish(event: BukkitUserVanishEvent) {
        val user = event.user
        if (!isActive(user)) return
        if (user.hasPermission(Permission.FLY)) {
            user.player()?.allowFlight = true
            user.player()?.isFlying = true
        }
    }

    @EventHandler
    private fun onUnVanish(event: BukkitUserUnVanishEvent) {
        val user = event.user
        if (!isActive(user)) return
        if (!user.hasPermission(Permission.FLY_KEEP_AFTER_REAPPEAR) && disableOnReappear) {
            user.sendComponent(language.feature.flyDisabled)
            user.player()?.allowFlight = false
            user.player()?.isFlying = false
        }
    }

}