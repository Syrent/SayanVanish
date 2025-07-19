package org.sayandev.sayanvanish.bukkit.feature

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.bukkit.event.Listener
import org.sayandev.sayanvanish.api.feature.Feature
import org.sayandev.sayanvanish.api.feature.category.FeatureCategories
import org.sayandev.stickynote.bukkit.registerListener
import org.sayandev.stickynote.bukkit.unregisterListener

@Serializable
abstract class ListenedFeature(
    @Transient override val id: String = "@transient",
    @Transient override var enabled: Boolean = true,
    @Transient override val category: FeatureCategories = FeatureCategories.DEFAULT,
    @Transient override val critical: Boolean = false
) : Feature(id, enabled, category, critical), Listener {

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