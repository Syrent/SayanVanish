package org.sayandev.sayanvanish.bukkit.feature.features.hook

import net.pl3x.map.core.Pl3xMap
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.bukkit.api.event.BukkitUserUnVanishEvent
import org.sayandev.sayanvanish.bukkit.api.event.BukkitUserVanishEvent
import org.sayandev.sayanvanish.bukkit.feature.HookFeature
import org.sayandev.stickynote.bukkit.registerListener
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@RegisteredFeature
@ConfigSerializable
class FeatureHookPl3xMap: HookFeature("hook_pl3xmap", "Pl3xMap") {

    override fun enable() {
        if (hasPlugin()) {
            Pl3xMapHookImpl(this)
        }
        super.enable()
    }
}

private class Pl3xMapHookImpl(val feature: FeatureHookPl3xMap): Listener {

    init {
        registerListener(this)
    }

    @EventHandler
    private fun onVanish(event: BukkitUserVanishEvent) {
        val user = event.user
        if (!feature.isActive(user)) return
        user.player()?.uniqueId?.let { Pl3xMap.api().playerRegistry.get(it)?.setHidden(true, false) }
    }

    @EventHandler
    private fun onUnVanish(event: BukkitUserUnVanishEvent) {
        val user = event.user
        if (!feature.isActive(user)) return
        user.player()?.uniqueId?.let { Pl3xMap.api().playerRegistry.get(it)?.setHidden(false, false) }
    }
}