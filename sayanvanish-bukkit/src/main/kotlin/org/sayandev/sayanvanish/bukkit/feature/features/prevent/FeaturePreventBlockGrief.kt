package org.sayandev.sayanvanish.bukkit.feature.features.prevent

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.api.feature.category.FeatureCategories
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.user
import org.sayandev.sayanvanish.bukkit.feature.ListenedFeature
import org.sayandev.stickynote.lib.spongepowered.configurate.objectmapping.ConfigSerializable

@RegisteredFeature
@ConfigSerializable
class FeaturePreventBlockGrief: ListenedFeature("prevent_block_grief", category = FeatureCategories.PREVENTION) {

    @EventHandler
    private fun onChangeBlock(event: EntityChangeBlockEvent) {
        if (!isActive()) return
        val user = (event.entity as? Player)?.user() ?: return
        if (user.isVanished) {
            event.isCancelled = true
        }
    }

}