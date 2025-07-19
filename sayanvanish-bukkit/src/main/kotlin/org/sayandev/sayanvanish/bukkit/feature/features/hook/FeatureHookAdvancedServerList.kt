package org.sayandev.sayanvanish.bukkit.feature.features.hook

import kotlinx.serialization.SerialName
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.bukkit.feature.HookFeature
import kotlinx.serialization.Serializable

@RegisteredFeature
@Serializable
@SerialName("hook_advanced_server_list")
class FeatureHookAdvancedServerList(
) : HookFeature("hook_advanced_server_list", "AdvancedServerList") {
    override fun enable() {
        if (hasPlugin()) {
            AdvancedServerListImpl().register()
        }
        super.enable()
    }
}