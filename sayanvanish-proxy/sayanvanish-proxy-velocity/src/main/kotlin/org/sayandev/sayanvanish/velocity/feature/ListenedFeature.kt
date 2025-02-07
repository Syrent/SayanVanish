package org.sayandev.sayanvanish.velocity.feature

import org.sayandev.sayanvanish.api.feature.Feature
import org.sayandev.sayanvanish.api.feature.category.FeatureCategories
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.sayandev.stickynote.velocity.registerListener
import org.spongepowered.configurate.serialize.TypeSerializerCollection

@ConfigSerializable
abstract class ListenedFeature(
    id: String,
    enabled: Boolean = true,
    category: FeatureCategories = FeatureCategories.DEFAULT,
    additionalSerializers: TypeSerializerCollection = TypeSerializerCollection.defaults()
) : Feature(id, enabled, category, additionalSerializers) {

    override fun enable() {
        if (!condition) return
        registerListener(this)
        super.enable()
    }

    override fun disable(reload: Boolean) {
        super.disable(reload)
    }

}