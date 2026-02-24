package org.sayandev.sayanvanish.paper.feature.features.hook

import kotlinx.serialization.SerialName
import net.pl3x.map.core.Pl3xMap
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.paper.api.event.PaperUserUnVanishEvent
import org.sayandev.sayanvanish.paper.api.event.PaperUserVanishEvent
import org.sayandev.sayanvanish.paper.feature.HookFeature
import org.sayandev.stickynote.bukkit.registerListener
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@RegisteredFeature
@Serializable
@SerialName("hook_pl3xmap")
class FeatureHookPl3xMap: HookFeature() {

    @Transient override val id = "hook_pl3xmap"
    override var enabled: Boolean = true
    override val plugin: String = "Pl3xMap"

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
    private fun onVanish(event: PaperUserVanishEvent) {
        val user = event.user
        if (!feature.isActive(user)) return
        Pl3xMap.api().playerRegistry.get(user.uniqueId)?.setHidden(true, false)
    }

    @EventHandler
    private fun onUnVanish(event: PaperUserUnVanishEvent) {
        val user = event.user
        if (!feature.isActive(user)) return
        Pl3xMap.api().playerRegistry.get(user.uniqueId)?.setHidden(false, false)
    }
}