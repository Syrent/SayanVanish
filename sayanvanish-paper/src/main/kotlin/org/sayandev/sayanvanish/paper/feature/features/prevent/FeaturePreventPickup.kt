package org.sayandev.sayanvanish.paper.feature.features.prevent

import kotlinx.serialization.SerialName
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityPickupItemEvent
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.api.feature.category.FeatureCategories
import org.sayandev.sayanvanish.paper.api.SayanVanishBukkitAPI.Companion.cachedVanishUser
import org.sayandev.sayanvanish.paper.feature.ListenedFeature
import org.sayandev.stickynote.bukkit.utils.ServerVersion
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@RegisteredFeature
@Serializable
@SerialName("prevent_pickup")
class FeaturePreventPickup: ListenedFeature() {

    @Transient override val id = "prevent_pickup"
    override var enabled: Boolean = true
    @Transient override val category: FeatureCategories = FeatureCategories.PREVENTION

    override var condition: Boolean = ServerVersion.supports(9)

    @EventHandler
    private fun onPickupItem(event: EntityPickupItemEvent) {
        val user = (event.entity as? Player)?.cachedVanishUser() ?: return
        if (!isActive(user)) return
        if (user.isVanished) {
            event.isCancelled = true
        }
    }
}