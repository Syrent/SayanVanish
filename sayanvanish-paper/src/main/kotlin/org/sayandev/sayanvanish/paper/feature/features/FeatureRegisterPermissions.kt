package org.sayandev.sayanvanish.paper.feature.features

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent
import org.sayandev.sayanvanish.api.Permissions
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.paper.feature.ListenedFeature
import org.sayandev.stickynote.bukkit.plugin

@RegisteredFeature
@Serializable
@SerialName("register_permissions")
class FeatureRegisterPermissions: ListenedFeature() {

    @Transient override val id = "register_permissions"
    override var enabled: Boolean = true
    @Transient override val critical: Boolean = false
    @Transient var registeredPermissions = false

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        if (registeredPermissions) return
        for (i in 1..100) {
            player.hasPermission("${plugin.name}.level.${i}")
        }
        for (permission in Permissions.entries) {
            player.hasPermission(permission.permission())
        }

        registeredPermissions = true

        return
    }

}
