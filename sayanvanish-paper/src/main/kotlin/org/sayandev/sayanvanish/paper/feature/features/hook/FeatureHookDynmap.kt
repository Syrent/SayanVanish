package org.sayandev.sayanvanish.paper.feature.features.hook

import kotlinx.serialization.SerialName
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.dynmap.DynmapAPI
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.paper.api.event.PaperUserUnVanishEvent
import org.sayandev.sayanvanish.paper.api.event.PaperUserVanishEvent
import org.sayandev.sayanvanish.paper.feature.HookFeature
import org.sayandev.stickynote.bukkit.registerListener
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@RegisteredFeature
@Serializable
@SerialName("hook_dynmap")
class FeatureHookDynmap : HookFeature() {

    @Transient override val id = "hook_dynmap"
    override var enabled: Boolean = true
    override val plugin: String = "dynmap"

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
    private fun onVanish(event: PaperUserVanishEvent) {
        val user = event.user
        if (!feature.isActive(user)) return
        api.setPlayerVisiblity(user.username, false)
    }

    @EventHandler
    private fun onUnVanish(event: PaperUserUnVanishEvent) {
        val user = event.user
        if (!feature.isActive(user)) return
        api.setPlayerVisiblity(user.username, true)
    }
}