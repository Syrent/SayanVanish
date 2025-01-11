package org.sayandev.sayanvanish.velocity.feature.features.hook

import me.neznamy.tab.api.TabPlayer
import me.neznamy.tab.api.integration.VanishIntegration
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.velocity.api.SayanVanishVelocityAPI
import org.sayandev.sayanvanish.velocity.feature.HookFeature
import org.sayandev.stickynote.velocity.plugin
import org.sayandev.stickynote.velocity.warn
import org.spongepowered.configurate.objectmapping.ConfigSerializable


@RegisteredFeature
@ConfigSerializable
class FeatureHookTAB: HookFeature("hook_tab", "tab") {

    override fun enable() {
        if (hasPlugin()) {
            VanishIntegrationTAB().register()
        }
        super.enable()
    }

    override fun disable() {
        if (hasPlugin()) {
            VanishIntegrationTAB().unregister()
        }
        super.disable()
    }
}

class VanishIntegrationTAB: VanishIntegration(plugin.container.description.name.get()) {
    override fun isVanished(player: TabPlayer): Boolean {
        return SayanVanishVelocityAPI.getInstance().isVanished(player.uniqueId)
    }

    override fun canSee(viewer: TabPlayer, target: TabPlayer): Boolean {
        val viewerUser = SayanVanishVelocityAPI.getInstance().getUser(viewer.uniqueId) ?: return true
        val targetUser = SayanVanishVelocityAPI.getInstance().getUser(target.uniqueId) ?: return true
        return SayanVanishVelocityAPI.getInstance().canSee(viewerUser, targetUser)
    }
}