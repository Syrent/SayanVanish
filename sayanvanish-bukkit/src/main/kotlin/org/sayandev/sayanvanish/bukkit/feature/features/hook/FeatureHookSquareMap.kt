package org.sayandev.sayanvanish.bukkit.feature.features.hook

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.bukkit.api.event.BukkitUserUnVanishEvent
import org.sayandev.sayanvanish.bukkit.api.event.BukkitUserVanishEvent
import org.sayandev.sayanvanish.bukkit.feature.HookFeature
import org.sayandev.stickynote.bukkit.registerListener
import org.spongepowered.configurate.objectmapping.ConfigSerializable
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

private class SquaremapHookImpl(val feature: FeatureHookSquareMap): Listener {

    init {
        registerListener(this)
    }

    @EventHandler
    private fun onVanish(event: BukkitUserVanishEvent) {
        val user = event.user
        if (!feature.isActive(user)) return
        user.player()?.uniqueId?.let { SquaremapProvider.get().playerManager().hide(it, true) }
    }

    @EventHandler
    private fun onUnVanish(event: BukkitUserUnVanishEvent) {
        val user = event.user
        if (!feature.isActive(user)) return
        user.player()?.uniqueId?.let { SquaremapProvider.get().playerManager().show(it, true) }
    }
}