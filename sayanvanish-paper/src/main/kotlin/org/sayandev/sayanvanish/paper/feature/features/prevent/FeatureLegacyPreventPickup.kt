package org.sayandev.sayanvanish.paper.feature.features.prevent

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerPickupItemEvent
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.api.feature.category.FeatureCategories
import org.sayandev.sayanvanish.paper.api.SayanVanishBukkitAPI.Companion.cachedVanishUser
import org.sayandev.sayanvanish.paper.feature.ListenedFeature
import org.sayandev.stickynote.bukkit.utils.ServerVersion

@RegisteredFeature
@Serializable
@SerialName("legacy_prevent_pickup")
class FeatureLegacyPreventPickup: ListenedFeature() {

    @Transient override val id = "legacy_prevent_pickup"
    override var enabled: Boolean = true
    @Transient override val category: FeatureCategories = FeatureCategories.PREVENTION

    override var condition: Boolean = !ServerVersion.supports(9)

    @EventHandler
    @Suppress("DEPRECATION")
    private fun onPickupItem(event: PlayerPickupItemEvent) {
        val user = event.player.cachedVanishUser() ?: return
        if (!isActive(user)) return
        if (user.isVanished) {
            event.isCancelled = true
        }
    }
}