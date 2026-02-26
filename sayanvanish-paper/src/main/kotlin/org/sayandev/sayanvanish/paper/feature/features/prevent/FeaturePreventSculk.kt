package org.sayandev.sayanvanish.paper.feature.features.prevent

import kotlinx.serialization.SerialName
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockReceiveGameEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.sayandev.sayanvanish.api.feature.Configurable
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.api.feature.category.FeatureCategories
import org.sayandev.sayanvanish.paper.api.SayanVanishPaperAPI.Companion.cachedVanishUser
import org.sayandev.sayanvanish.paper.feature.ListenedFeature
import org.sayandev.stickynote.bukkit.utils.ServerVersion
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@RegisteredFeature
@Serializable
@SerialName("prevent_sculk")
class FeaturePreventSculk(
    @Configurable val preventSculkSensor: Boolean = true,
    @Configurable val preventShriek: Boolean = true
): ListenedFeature() {

    @Transient override val id = "prevent_sculk"
    override var enabled: Boolean = true
    @Transient override val category: FeatureCategories = FeatureCategories.PREVENTION

    val sculkBlocks = listOf(
        "SCULK_SENSOR",
        "CALIBRATED_SCULK_SENSOR",
        "SCULK_SHRIEKER"
    )

    @Transient
    override var condition: Boolean = ServerVersion.supports(19)

    @EventHandler(priority = EventPriority.HIGH)
    private fun cancelSculkSensor(event: BlockReceiveGameEvent) {
        if (!preventSculkSensor) return
        val player = event.entity as? Player ?: return
        val user = player.cachedVanishUser() ?: return
        if (!user.isVanished) return
        if (!isActive(user)) return
        event.isCancelled = true
    }

    @EventHandler
    private fun cancelShriek(event: PlayerInteractEvent) {
        if (!preventShriek) return
        val block = event.clickedBlock ?: return
        if (event.action != Action.PHYSICAL) return
        if (!sculkBlocks.contains(block.type.name)) return
        val player = event.player
        val user = player.cachedVanishUser() ?: return
        if (!user.isVanished) return
        if (!isActive(user)) return
        event.isCancelled = true
    }

}