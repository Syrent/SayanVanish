package org.sayandev.sayanvanish.bukkit.feature.features.prevent

import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockPlaceEvent
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.api.feature.category.FeatureCategories
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.user
import org.sayandev.sayanvanish.bukkit.feature.ListenedFeature
import org.sayandev.stickynote.lib.spongepowered.configurate.objectmapping.ConfigSerializable

@RegisteredFeature
@ConfigSerializable
class FeaturePreventBlockPlace: ListenedFeature("prevent_block_place", category = FeatureCategories.PREVENTION) {

    @EventHandler
    private fun onBlockPlace(event: BlockPlaceEvent) {
        if (!isActive()) return
        val user = event.player.user() ?: return
        if (user.isVanished) {
            event.isCancelled = true
        }
    }

}