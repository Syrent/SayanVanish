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
import kotlinx.serialization.SerialName
import kotlinx.serialization.Transient


@RegisteredFeature
@Serializable
@SerialName("hook_tab")
class FeatureHookTAB : HookFeature() {

    @Transient override val id = "hook_tab"
    override var enabled: Boolean = true
    override val plugin: String = "TAB"

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
        return VanishAPI.get().getCacheService().getVanishUsers()[player.uniqueId]?.isVanished == true
    }

    override fun canSee(viewer: TabPlayer, target: TabPlayer): Boolean {
        if (viewer.uniqueId == target.uniqueId) return true
        val viewerUser = VanishAPI.get().getCacheService().getVanishUsers()[viewer.uniqueId] ?: VelocityVanishUser(viewer.uniqueId, viewer.name)
        val targetUser = VanishAPI.get().getCacheService().getVanishUsers()[target.uniqueId] ?: return true
        return VanishAPI.get().canSee(viewerUser, targetUser)
    }
}