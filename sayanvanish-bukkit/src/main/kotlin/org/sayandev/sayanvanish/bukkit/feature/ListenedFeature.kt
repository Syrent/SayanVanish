package org.sayandev.sayanvanish.bukkit.feature

import org.bukkit.event.Listener
import org.sayandev.sayanvanish.api.feature.Feature
import org.sayandev.sayanvanish.api.feature.category.FeatureCategories
import org.sayandev.stickynote.bukkit.registerListener
import org.sayandev.stickynote.bukkit.unregisterListener
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.serialize.TypeSerializerCollection

@ConfigSerializable
abstract class ListenedFeature(
    id: String,
    enabled: Boolean = true,
    category: FeatureCategories = FeatureCategories.DEFAULT,
    additionalSerializers: TypeSerializerCollection = TypeSerializerCollection.defaults(),
    critical: Boolean = false
) : Feature(id, enabled, category, additionalSerializers, critical), Listener {

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