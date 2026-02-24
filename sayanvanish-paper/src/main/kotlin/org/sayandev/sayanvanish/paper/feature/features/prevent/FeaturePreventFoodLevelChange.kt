package org.sayandev.sayanvanish.paper.feature.features.prevent

import kotlinx.serialization.SerialName
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.api.feature.category.FeatureCategories
import org.sayandev.sayanvanish.paper.api.SayanVanishBukkitAPI.Companion.cachedVanishUser
import org.sayandev.sayanvanish.paper.feature.ListenedFeature
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@RegisteredFeature
@Serializable
@SerialName("prevent_food_level_change")
class FeaturePreventFoodLevelChange(
    val ignoreIfIncrease: Boolean = true
): ListenedFeature() {

    @Transient override val id = "prevent_food_level_change"
    override var enabled: Boolean = true
    @Transient override val category: FeatureCategories = FeatureCategories.PREVENTION

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