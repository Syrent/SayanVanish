package org.sayandev.sayanvanish.velocity.feature

import org.sayandev.sayanvanish.api.User
import org.sayandev.sayanvanish.api.feature.category.FeatureCategories
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.sayandev.stickynote.velocity.hasPlugin

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

    override fun isActive(user: User): Boolean {
        return super.isActive(user) && hasPlugin()
    }

}