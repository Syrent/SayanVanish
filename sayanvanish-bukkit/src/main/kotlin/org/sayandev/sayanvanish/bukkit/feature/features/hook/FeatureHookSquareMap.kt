package org.sayandev.sayanvanish.bukkit.feature.features.hook

import org.bukkit.event.EventHandler
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.bukkit.api.event.BukkitUserUnVanishEvent
import org.sayandev.sayanvanish.bukkit.api.event.BukkitUserVanishEvent
import org.sayandev.sayanvanish.bukkit.feature.HookFeature
import org.sayandev.stickynote.lib.spongepowered.configurate.objectmapping.ConfigSerializable
import xyz.jpenilla.squaremap.api.SquaremapProvider

@RegisteredFeature
@ConfigSerializable
class FeatureHookSquareMap: HookFeature("hook_squaremap", "squaremap") {

    override fun enable() {
        if (hasPlugin()) {
            SquaremapHookImpl(this)
        }
        super.enable()
    }
}

private class SquaremapHookImpl(val feature: FeatureHookSquareMap) {
    @EventHandler
    private fun onVanish(event: BukkitUserVanishEvent) {
        if (!feature.isActive()) return
        val user = event.user
        user.player()?.uniqueId?.let { SquaremapProvider.get().playerManager().hide(it, true) }
    }

    @EventHandler
    private fun onUnVanish(event: BukkitUserUnVanishEvent) {
        if (!feature.isActive()) return
        val user = event.user
        user.player()?.uniqueId?.let { SquaremapProvider.get().playerManager().show(it, true) }
    }
}