package org.sayandev.sayanvanish.bukkit.feature.features.hook

import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.bukkit.feature.HookFeature
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@RegisteredFeature
@ConfigSerializable
class FeatureHookAdvancedServerList : HookFeature("hook_advanced_server_list", "AdvancedServerList") {
    override fun enable() {
        if (hasPlugin()) {
            AdvancedServerListImpl().register()
        }
        super.enable()
    }
}