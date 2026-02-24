package org.sayandev.sayanvanish.paper.feature.features.prevent

import kotlinx.serialization.SerialName
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockPlaceEvent
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.api.feature.category.FeatureCategories
import org.sayandev.sayanvanish.paper.api.SayanVanishBukkitAPI.Companion.cachedVanishUser
import org.sayandev.sayanvanish.paper.feature.ListenedFeature
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@RegisteredFeature
@Serializable
@SerialName("prevent_block_place")
class FeaturePreventBlockPlace: ListenedFeature() {

    @Transient override val id = "prevent_block_place"
    override var enabled: Boolean = false
    @Transient override val category: FeatureCategories = FeatureCategories.PREVENTION

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onBlockPlace(event: BlockPlaceEvent) {
        val user = event.player.cachedVanishUser() ?: return
        if (!isActive(user)) return
        if (user.isVanished) {
            event.isCancelled = true
        }
    }

}