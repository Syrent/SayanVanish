package org.sayandev.sayanvanish.velocity.feature.features.hook

import me.neznamy.tab.api.TabPlayer
import me.neznamy.tab.api.integration.VanishIntegration
import org.sayandev.sayanvanish.api.VanishAPI
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.velocity.api.SayanVanishVelocityAPI
import org.sayandev.sayanvanish.velocity.api.VelocityVanishUser
import org.sayandev.sayanvanish.velocity.feature.HookFeature
import org.sayandev.stickynote.velocity.plugin
import kotlinx.serialization.Serializable
import com.charleskorn.kaml.YamlComment


@RegisteredFeature
@Serializable
class FeatureHookTAB(
    @YamlComment("Whether to use cache data for vanish status. This will improve performance but may cause a small delay in tablist removal after join.")
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
        return VanishAPI.get().getDatabase().getCachedVanishUser(player.uniqueId)?.isVanished == true
    }

    override fun canSee(viewer: TabPlayer, target: TabPlayer): Boolean {
        if (viewer.uniqueId == target.uniqueId) return true
        val viewerUser = VanishAPI.get().getDatabase().getVanishUserCache(viewer.uniqueId) ?: VelocityVanishUser(viewer.uniqueId, viewer.name)
        val targetUser = VanishAPI.get().getDatabase().getVanishUserCache(target.uniqueId) ?: return true
        return VanishAPI.get().canSee(viewerUser, targetUser)
    }
}