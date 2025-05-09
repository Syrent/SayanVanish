package org.sayandev.sayanvanish.bukkit.feature.features.prevent

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.api.feature.category.FeatureCategories
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.user
import org.sayandev.sayanvanish.bukkit.feature.ListenedFeature
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@RegisteredFeature
@ConfigSerializable
class PreventFoodLevelChange(
    val ignoreIfIncrease: Boolean = true
): ListenedFeature("prevent_food_level_change", category = FeatureCategories.PREVENTION) {

    @EventHandler
    private fun onBlockBreak(event: FoodLevelChangeEvent) {
        if (ignoreIfIncrease && event.foodLevel > event.entity.foodLevel) return
        val player = event.entity as? Player ?: return
        val user = player.user() ?: return
        if (!isActive(user)) return
        if (user.isVanished) {
            event.isCancelled = true
        }
    }

}