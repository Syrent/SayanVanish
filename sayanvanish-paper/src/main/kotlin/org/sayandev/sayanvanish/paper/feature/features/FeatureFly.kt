package org.sayandev.sayanvanish.paper.feature.features

import org.bukkit.event.EventHandler
import org.sayandev.sayanvanish.api.Permissions
import org.sayandev.sayanvanish.api.feature.Configurable
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.paper.api.event.PaperUserUnVanishEvent
import org.sayandev.sayanvanish.paper.api.event.PaperUserVanishEvent
import org.sayandev.sayanvanish.paper.config.language
import org.sayandev.sayanvanish.paper.feature.ListenedFeature
import kotlinx.serialization.Serializable
import com.charleskorn.kaml.YamlComment
import kotlinx.serialization.SerialName
import kotlinx.serialization.Transient

@RegisteredFeature
@Serializable
@SerialName("fly")
class FeatureFly(
    @YamlComment("Disable fly when player reappears and don't have keep fly permission.")
    @Configurable val disableOnReappear: Boolean = true
) : ListenedFeature() {

    @Transient override val id = "fly"
    override var enabled: Boolean = true

    @EventHandler
    private fun onVanish(event: PaperUserVanishEvent) {
        val user = event.user
        if (!isActive(user)) return
        if (user.hasPermission(Permissions.FLY)) {
            user.player()?.allowFlight = true
            user.player()?.isFlying = true
        }
    }

    @EventHandler
    private fun onUnVanish(event: PaperUserUnVanishEvent) {
        val user = event.user
        if (!isActive(user)) return
        if (!user.hasPermission(Permissions.FLY_KEEP_AFTER_REAPPEAR) && disableOnReappear) {
            user.sendMessageWithPrefix(language.feature.flyDisabled)
            user.player()?.allowFlight = false
            user.player()?.isFlying = false
        }
    }

}