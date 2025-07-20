package org.sayandev.sayanvanish.bukkit.feature.features

import org.bukkit.event.EventHandler
import org.sayandev.sayanvanish.api.Permission
import org.sayandev.sayanvanish.api.feature.Configurable
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.bukkit.api.event.BukkitUserUnVanishEvent
import org.sayandev.sayanvanish.bukkit.api.event.BukkitUserVanishEvent
import org.sayandev.sayanvanish.bukkit.config.language
import org.sayandev.sayanvanish.bukkit.feature.ListenedFeature
import org.sayandev.stickynote.bukkit.utils.AdventureUtils.component
import kotlinx.serialization.Serializable
import com.charleskorn.kaml.YamlComment
import kotlinx.serialization.SerialName

@RegisteredFeature
@Serializable
@SerialName("fly")
class FeatureFly(
    override var enabled: Boolean = true,
    @YamlComment("Disable fly when player reappears and don't have keep fly permission.")
    @Configurable val disableOnReappear: Boolean = true
) : ListenedFeature("fly", enabled) {

    @EventHandler
    private fun onVanish(event: BukkitUserVanishEvent) {
        val user = event.user
        if (!isActive(user)) return
        if (user.hasPermission(Permission.FLY)) {
            user.player()?.allowFlight = true
            user.player()?.isFlying = true
        }
    }

    @EventHandler
    private fun onUnVanish(event: BukkitUserUnVanishEvent) {
        val user = event.user
        if (!isActive(user)) return
        if (!user.hasPermission(Permission.FLY_KEEP_AFTER_REAPPEAR) && disableOnReappear) {
            user.sendMessage(language.feature.flyDisabled.component())
            user.player()?.allowFlight = false
            user.player()?.isFlying = false
        }
    }

}