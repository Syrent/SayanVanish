package org.sayandev.sayanvanish.bungeecord.feature

import net.md_5.bungee.api.plugin.Listener
import org.sayandev.sayanvanish.api.feature.Feature
import org.sayandev.sayanvanish.api.feature.category.FeatureCategories
import org.sayandev.stickynote.bungeecord.StickyNote.registerListener
import kotlinx.serialization.Serializable
import org.spongepowered.configurate.serialize.TypeSerializerCollection

@Serializable
abstract class ListenedFeature(
    id: String,
    enabled: Boolean = true,
    category: FeatureCategories = FeatureCategories.DEFAULT,
    additionalSerializers: TypeSerializerCollection = TypeSerializerCollection.defaults()
) : Feature(id, enabled, category, additionalSerializers), Listener {

    override fun enable() {
        if (!condition) return
        registerListener(this)
        super.enable()
    }

    override fun disable(reload: Boolean) {
        super.disable(reload)
    }

}