package org.sayandev.sayanvanish.bukkit.feature.features.prevent

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.player.PlayerPickupItemEvent
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.api.feature.category.FeatureCategories
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.user
import org.sayandev.sayanvanish.bukkit.feature.ListenedFeature
import org.sayandev.stickynote.bukkit.utils.ServerVersion
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@RegisteredFeature
@ConfigSerializable
class FeaturePreventPickup: ListenedFeature("prevent_pickup", category = FeatureCategories.PREVENTION) {

    override var condition: Boolean = ServerVersion.supports(9)

    @EventHandler
    private fun onPickupItem(event: EntityPickupItemEvent) {
        val user = (event.entity as? Player)?.user() ?: return
        if (!isActive(user)) return
        if (user.isVanished) {
            event.isCancelled = true
        }
    }
}

@RegisteredFeature
@ConfigSerializable
class FeatureLegacyPreventPickup: ListenedFeature("legacy_prevent_pickup", category = FeatureCategories.PREVENTION) {

    override var condition: Boolean = !ServerVersion.supports(9)

    @EventHandler
    @Suppress("DEPRECATION")
    private fun onPickupItem(event: PlayerPickupItemEvent) {
        val user = event.player.user() ?: return
        if (!isActive(user)) return
        if (user.isVanished) {
            event.isCancelled = true
        }
    }
}