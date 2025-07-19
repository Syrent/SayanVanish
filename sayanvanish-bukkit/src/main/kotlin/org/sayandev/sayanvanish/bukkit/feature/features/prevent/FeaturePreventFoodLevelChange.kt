package org.sayandev.sayanvanish.bukkit.feature.features.prevent

import kotlinx.serialization.SerialName
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.api.feature.category.FeatureCategories
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.cachedVanishUser
import org.sayandev.sayanvanish.bukkit.feature.ListenedFeature
import kotlinx.serialization.Serializable

@RegisteredFeature
@Serializable
@SerialName("prevent_food_level_change")
class FeaturePreventFoodLevelChange(
    val ignoreIfIncrease: Boolean = true
): ListenedFeature("prevent_food_level_change", category = FeatureCategories.PREVENTION) {

    @EventHandler
    private fun onBlockBreak(event: FoodLevelChangeEvent) {
        if (ignoreIfIncrease && event.foodLevel > event.entity.foodLevel) return
        val player = event.entity as? Player ?: return
        val user = player.cachedVanishUser() ?: return
        if (!isActive(user)) return
        if (user.isVanished) {
            event.isCancelled = true
        }
    }

}