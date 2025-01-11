package org.sayandev.sayanvanish.bukkit.feature.features.hook

import me.neznamy.tab.api.TabPlayer
import me.neznamy.tab.api.integration.VanishIntegration
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI
import org.sayandev.sayanvanish.bukkit.feature.HookFeature
import org.sayandev.stickynote.bukkit.plugin
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@RegisteredFeature
@ConfigSerializable
class FeatureHookTAB: HookFeature("hook_tab", "TAB") {
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

private class VanishIntegrationTAB: VanishIntegration(plugin.name) {
    override fun isVanished(player: TabPlayer): Boolean {
        return SayanVanishBukkitAPI.getInstance().isVanished(player.uniqueId)
    }

    override fun canSee(viewer: TabPlayer, target: TabPlayer): Boolean {
        val viewerUser = SayanVanishBukkitAPI.getInstance().getUser(viewer.uniqueId)
        val targetUser = SayanVanishBukkitAPI.getInstance().getUser(target.uniqueId) ?: return true
        return SayanVanishBukkitAPI.getInstance().canSee(viewerUser, targetUser)
    }
}