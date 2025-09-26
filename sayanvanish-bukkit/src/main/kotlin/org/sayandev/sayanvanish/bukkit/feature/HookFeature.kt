package org.sayandev.sayanvanish.bukkit.feature

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.sayandev.sayanvanish.api.User
import org.sayandev.sayanvanish.api.feature.category.FeatureCategories
import org.sayandev.stickynote.bukkit.hasPlugin

@Serializable
abstract class HookFeature: ListenedFeature() {
    abstract val plugin: String
    @Transient override var enabled: Boolean = true
    override val category: FeatureCategories = FeatureCategories.HOOK

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