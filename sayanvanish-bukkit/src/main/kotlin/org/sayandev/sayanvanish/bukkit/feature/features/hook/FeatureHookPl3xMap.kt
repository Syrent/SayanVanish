package org.sayandev.sayanvanish.bukkit.feature.features.hook

import kotlinx.serialization.SerialName
import net.pl3x.map.core.Pl3xMap
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.bukkit.api.event.BukkitUserUnVanishEvent
import org.sayandev.sayanvanish.bukkit.api.event.BukkitUserVanishEvent
import org.sayandev.sayanvanish.bukkit.feature.HookFeature
import org.sayandev.stickynote.bukkit.registerListener
import kotlinx.serialization.Serializable

@RegisteredFeature
@Serializable
@SerialName("hook_pl3xmap")
class FeatureHookPl3xMap(
    override var enabled: Boolean = true,
): HookFeature("hook_pl3xmap", "Pl3xMap", enabled) {

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
        Pl3xMap.api().playerRegistry.get(user.uniqueId)?.setHidden(true, false)
    }

    @EventHandler
    private fun onUnVanish(event: BukkitUserUnVanishEvent) {
        val user = event.user
        if (!feature.isActive(user)) return
        Pl3xMap.api().playerRegistry.get(user.uniqueId)?.setHidden(false, false)
    }
}