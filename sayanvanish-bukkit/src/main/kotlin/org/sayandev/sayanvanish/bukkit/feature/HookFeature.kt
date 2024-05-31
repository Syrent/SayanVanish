package org.sayandev.sayanvanish.bukkit.feature

import org.sayandev.sayanvanish.api.feature.category.FeatureCategories
import org.sayandev.stickynote.bukkit.hasPlugin
import org.sayandev.stickynote.bukkit.registerListener
import org.sayandev.stickynote.bukkit.unregisterListener
import org.sayandev.stickynote.lib.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
abstract class HookFeature(
    id: String,
    val plugin: String,
    enabled: Boolean = true,
    category: FeatureCategories = FeatureCategories.HOOK,
) : ListenedFeature(id, enabled, category) {

    override fun enable() {
        if (!condition) return
        registerListener(this)
        super.enable()
    }

    override fun disable() {
        unregisterListener(this)
        super.disable()
    }

    fun hasPlugin(): Boolean {
        return hasPlugin(plugin)
    }

    override fun isActive(): Boolean {
        return super.isActive() && hasPlugin()
    }


}