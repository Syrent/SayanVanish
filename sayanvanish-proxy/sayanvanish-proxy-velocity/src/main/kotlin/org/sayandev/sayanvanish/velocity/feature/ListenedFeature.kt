package org.sayandev.sayanvanish.velocity.feature

import kotlinx.serialization.Serializable
import org.sayandev.sayanvanish.api.feature.Feature
import org.sayandev.stickynote.velocity.registerListener

@Serializable
abstract class ListenedFeature : Feature() {

    override fun enable() {
        if (!condition) return
        registerListener(this)
        super.enable()
    }

    override fun disable(reload: Boolean) {
        super.disable(reload)
    }

}