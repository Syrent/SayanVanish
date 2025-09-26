package org.sayandev.sayanvanish.bukkit.feature.features.hook

import kotlinx.serialization.SerialName
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.bukkit.feature.HookFeature
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@RegisteredFeature
@Serializable
@SerialName("hook_advanced_server_list")
class FeatureHookAdvancedServerList: HookFeature() {

    @Transient override val id = "hook_advanced_server_list"
    override var enabled: Boolean = true
    override val plugin: String = "AdvancedServerList"

    override fun enable() {
        if (hasPlugin()) {
            AdvancedServerListImpl().register()
        }
        super.enable()
    }
}