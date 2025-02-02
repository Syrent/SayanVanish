package org.sayandev.sayanvanish.bukkit.feature.features.hook

import me.neznamy.tab.api.TabPlayer
import me.neznamy.tab.api.integration.VanishIntegration
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI
import org.sayandev.sayanvanish.bukkit.feature.HookFeature
import org.sayandev.stickynote.bukkit.plugin
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

    override fun disable() {
        if (hasPlugin()) {
            VanishIntegrationTAB(this).unregister()
        }
        super.disable()
    }
}

private class VanishIntegrationTAB(val feature: FeatureHookTAB): VanishIntegration(plugin.name) {
    override fun isVanished(player: TabPlayer): Boolean {
        return SayanVanishBukkitAPI.getInstance().isVanished(player.uniqueId, feature.useCacheData)
    }

    override fun canSee(viewer: TabPlayer, target: TabPlayer): Boolean {
        val viewerUser = SayanVanishBukkitAPI.getInstance().getUser(viewer.uniqueId, feature.useCacheData)
        val targetUser = SayanVanishBukkitAPI.getInstance().getUser(target.uniqueId, feature.useCacheData) ?: return true
        return SayanVanishBukkitAPI.getInstance().canSee(viewerUser, targetUser)
    }
}