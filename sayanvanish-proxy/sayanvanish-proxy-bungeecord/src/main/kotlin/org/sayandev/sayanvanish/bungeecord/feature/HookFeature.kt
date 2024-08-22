package org.sayandev.sayanvanish.bungeecord.feature

import org.sayandev.sayanvanish.api.BasicUser
import org.sayandev.sayanvanish.api.feature.category.FeatureCategories
import org.sayandev.stickynote.bungeecord.hasPlugin
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
abstract class HookFeature(
    id: String,
    @Transient val plugin: String,
    enabled: Boolean = true,
    category: FeatureCategories = FeatureCategories.HOOK,
) : ListenedFeature(id, enabled, category) {

    fun hasPlugin(): Boolean {
        return hasPlugin(plugin)
    }

    override fun isActive(): Boolean {
        return super.isActive() && hasPlugin()
    }

    override fun isActive(user: BasicUser): Boolean {
        return super.isActive(user) && hasPlugin()
    }

}