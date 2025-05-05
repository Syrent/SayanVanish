package org.sayandev.sayanvanish.velocity.feature.features.hook

import me.neznamy.tab.api.TabPlayer
import me.neznamy.tab.api.integration.VanishIntegration
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.velocity.api.SayanVanishVelocityAPI
import org.sayandev.sayanvanish.velocity.api.VelocityVanishUser
import org.sayandev.sayanvanish.velocity.feature.HookFeature
import org.sayandev.stickynote.velocity.plugin
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment


@RegisteredFeature
@ConfigSerializable
class FeatureHookTAB(
    @Comment("Whether to use cache data for vanish status. This will improve performance but may cause a small delay in tablist removal after join.")
    val useCacheData: Boolean = false
): HookFeature("hook_tab", "TAB") {
    override fun enable() {
        if (hasPlugin()) {
            VanishIntegrationTAB(this).register()
        }
        super.enable()
    }

    override fun disable(reload: Boolean) {
        if (hasPlugin()) {
            VanishIntegrationTAB(this).unregister()
        }
        super.disable(reload)
    }
}

private class VanishIntegrationTAB(val feature: FeatureHookTAB): VanishIntegration(plugin.container.description.name.get()) {
    override fun isVanished(player: TabPlayer): Boolean {
        return SayanVanishVelocityAPI.getInstance().isVanishedAsync(player.uniqueId, feature.useCacheData)
    }

    override fun canSee(viewer: TabPlayer, target: TabPlayer): Boolean {
        if (viewer.uniqueId == target.uniqueId) return true
        val viewerUser = SayanVanishVelocityAPI.getInstance().getUser(viewer.uniqueId, feature.useCacheData) ?: VelocityVanishUser(viewer.uniqueId, viewer.name)
        val targetUser = SayanVanishVelocityAPI.getInstance().getUser(target.uniqueId, feature.useCacheData) ?: return true
        return SayanVanishVelocityAPI.getInstance().canSee(viewerUser, targetUser)
    }
}