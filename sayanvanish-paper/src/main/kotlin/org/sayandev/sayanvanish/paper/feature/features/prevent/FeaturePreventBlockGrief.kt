package org.sayandev.sayanvanish.paper.feature.features.prevent

import kotlinx.serialization.SerialName
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.api.feature.category.FeatureCategories
import org.sayandev.sayanvanish.paper.api.SayanVanishBukkitAPI.Companion.cachedVanishUser
import org.sayandev.sayanvanish.paper.feature.ListenedFeature
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@RegisteredFeature
@Serializable
@SerialName("prevent_block_grief")
class FeaturePreventBlockGrief(
): ListenedFeature() {

    @Transient override val id = "prevent_block_grief"
    override var enabled: Boolean = false
    @Transient override val category: FeatureCategories = FeatureCategories.PREVENTION

    @EventHandler
    private fun onChangeBlock(event: EntityChangeBlockEvent) {
        val user = (event.entity as? Player)?.cachedVanishUser() ?: return
        if (!isActive(user)) return
        if (user.isVanished) {
            event.isCancelled = true
        }
    }

}