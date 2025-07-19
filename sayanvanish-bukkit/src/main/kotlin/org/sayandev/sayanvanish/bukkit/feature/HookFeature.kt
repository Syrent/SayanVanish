package org.sayandev.sayanvanish.bukkit.feature

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.sayandev.sayanvanish.api.User
import org.sayandev.sayanvanish.api.feature.category.FeatureCategories
import org.sayandev.stickynote.bukkit.hasPlugin

@Serializable
abstract class HookFeature(
    @Transient override val id: String = "@transient",
    @Transient val plugin: String = "@transient",
    @Transient override var enabled: Boolean = true,
    @Transient override val category: FeatureCategories = FeatureCategories.HOOK,
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