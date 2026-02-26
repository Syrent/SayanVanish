package org.sayandev.sayanvanish.paper.feature.features.hook

import me.neznamy.tab.api.TabPlayer
import me.neznamy.tab.api.integration.VanishIntegration
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.paper.api.PaperVanishUser
import org.sayandev.sayanvanish.paper.api.SayanVanishPaperAPI.Companion.cachedVanishUser
import org.sayandev.sayanvanish.paper.feature.HookFeature
import org.sayandev.stickynote.bukkit.plugin
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Transient

@RegisteredFeature
@Serializable
@SerialName("hook_tab")
class FeatureHookTAB: HookFeature() {

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

private class VanishIntegrationTAB(val feature: FeatureHookTAB): VanishIntegration(plugin.name) {
    override fun isVanished(player: TabPlayer): Boolean {
        return player.uniqueId.cachedVanishUser()?.isVanished ?: false
    }

    override fun canSee(viewer: TabPlayer, target: TabPlayer): Boolean {
        if (viewer.uniqueId == target.uniqueId) return true
        val targetUser = target.uniqueId.cachedVanishUser() ?: return true
        val viewerUser = viewer.uniqueId.cachedVanishUser() ?: PaperVanishUser(viewer.uniqueId, viewer.name)
        return viewerUser.canSee(targetUser)
    }
}