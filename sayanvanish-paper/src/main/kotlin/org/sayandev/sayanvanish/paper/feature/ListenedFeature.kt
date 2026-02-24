package org.sayandev.sayanvanish.paper.feature

import kotlinx.serialization.Serializable
import org.bukkit.event.Listener
import org.sayandev.sayanvanish.api.feature.Feature
import org.sayandev.stickynote.bukkit.registerListener
import org.sayandev.stickynote.bukkit.unregisterListener

@Serializable
abstract class ListenedFeature : Feature(), Listener {

    override fun enable() {
        if (!condition) return
        registerListener(this)
        super.enable()
    }

    override fun disable(reload: Boolean) {
        unregisterListener(this)
        super.disable(reload)
    }

}