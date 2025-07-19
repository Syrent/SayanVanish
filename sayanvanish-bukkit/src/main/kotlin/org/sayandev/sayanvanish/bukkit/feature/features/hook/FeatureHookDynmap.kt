package org.sayandev.sayanvanish.bukkit.feature.features.hook

import kotlinx.serialization.SerialName
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.dynmap.DynmapAPI
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.bukkit.api.event.BukkitUserUnVanishEvent
import org.sayandev.sayanvanish.bukkit.api.event.BukkitUserVanishEvent
import org.sayandev.sayanvanish.bukkit.feature.HookFeature
import org.sayandev.stickynote.bukkit.registerListener
import kotlinx.serialization.Serializable

@RegisteredFeature
@Serializable
@SerialName("hook_dynmap")
class FeatureHookDynmap: HookFeature("hook_dynmap", "dynmap") {

    override fun enable() {
        if (hasPlugin()) {
            DynmapHookImpl(this)
        }
        super.enable()
    }
}

private class DynmapHookImpl(val feature: FeatureHookDynmap): Listener {
    val api = Bukkit.getPluginManager().getPlugin("dynmap") as DynmapAPI

    init {
        registerListener(this)
    }

    @EventHandler
    private fun onVanish(event: BukkitUserVanishEvent) {
        val user = event.user
        if (!feature.isActive(user)) return
        api.setPlayerVisiblity(user.username, false)
    }

    @EventHandler
    private fun onUnVanish(event: BukkitUserUnVanishEvent) {
        val user = event.user
        if (!feature.isActive(user)) return
        api.setPlayerVisiblity(user.username, true)
    }
}