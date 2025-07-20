package org.sayandev.sayanvanish.bukkit.feature.features.prevent

import kotlinx.serialization.SerialName
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockPlaceEvent
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.api.feature.category.FeatureCategories
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.cachedVanishUser
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.user
import org.sayandev.sayanvanish.bukkit.feature.ListenedFeature
import kotlinx.serialization.Serializable

@RegisteredFeature
@Serializable
@SerialName("prevent_block_place")
class FeaturePreventBlockPlace(
    override var enabled: Boolean = false,
): ListenedFeature("prevent_block_place", enabled, category = FeatureCategories.PREVENTION) {

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onBlockPlace(event: BlockPlaceEvent) {
        val user = event.player.cachedVanishUser() ?: return
        if (!isActive(user)) return
        if (user.isVanished) {
            event.isCancelled = true
        }
    }

}