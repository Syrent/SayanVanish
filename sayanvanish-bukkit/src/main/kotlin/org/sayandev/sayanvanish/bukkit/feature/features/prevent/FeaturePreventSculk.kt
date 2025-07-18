package org.sayandev.sayanvanish.bukkit.feature.features.prevent

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockReceiveGameEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.sayandev.sayanvanish.api.feature.Configurable
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.api.feature.category.FeatureCategories
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.cachedVanishUser
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.user
import org.sayandev.sayanvanish.bukkit.feature.ListenedFeature
import org.sayandev.stickynote.bukkit.utils.ServerVersion
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@RegisteredFeature
@ConfigSerializable
class FeaturePreventSculk(
    @Configurable val preventSculkSensor: Boolean = true,
    @Configurable val preventShriek: Boolean = true
): ListenedFeature("prevent_sculk", category = FeatureCategories.PREVENTION) {

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