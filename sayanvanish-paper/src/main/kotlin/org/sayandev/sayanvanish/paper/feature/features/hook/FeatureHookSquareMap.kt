package org.sayandev.sayanvanish.paper.feature.features.hook

import kotlinx.serialization.SerialName
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.paper.api.event.PaperUserUnVanishEvent
import org.sayandev.sayanvanish.paper.api.event.PaperUserVanishEvent
import org.sayandev.sayanvanish.paper.feature.HookFeature
import org.sayandev.stickynote.bukkit.registerListener
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import xyz.jpenilla.squaremap.api.SquaremapProvider

@RegisteredFeature
@Serializable
@SerialName("hook_squaremap")
class FeatureHookSquareMap: HookFeature() {

    @Transient override val id = "hook_squaremap"
    override var enabled: Boolean = true
    override val plugin: String = "squaremap"

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
    private fun onVanish(event: PaperUserVanishEvent) {
        val user = event.user
        if (!feature.isActive(user)) return
        user.player()?.uniqueId?.let { SquaremapProvider.get().playerManager().hide(it, true) }
    }

    @EventHandler
    private fun onUnVanish(event: PaperUserUnVanishEvent) {
        val user = event.user
        if (!feature.isActive(user)) return
        user.player()?.uniqueId?.let { SquaremapProvider.get().playerManager().show(it, true) }
    }
}