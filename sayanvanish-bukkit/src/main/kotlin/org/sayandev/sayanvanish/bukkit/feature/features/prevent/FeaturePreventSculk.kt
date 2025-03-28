package org.sayandev.sayanvanish.bukkit.feature.features.prevent

import org.bukkit.GameEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.world.GenericGameEvent
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.api.feature.category.FeatureCategories
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.user
import org.sayandev.sayanvanish.bukkit.feature.ListenedFeature
import org.sayandev.stickynote.bukkit.utils.ServerVersion
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@RegisteredFeature
@ConfigSerializable
class FeaturePreventSculk(
    val preventSculkSensor: Boolean,
    val preventShriek: Boolean
): ListenedFeature("prevent_sculk", category = FeatureCategories.PREVENTION) {

    @Transient
    override var condition: Boolean = ServerVersion.supports(19)

    @EventHandler
    private fun cancelSculkSensor(event: GenericGameEvent) {
        if (!preventSculkSensor) return
        if (event.event != GameEvent.SCULK_SENSOR_TENDRILS_CLICKING) return
        val player = event.entity as? Player ?: return
        val user = player.user() ?: return
        if (!isActive(user)) return
        if (!user.isVanished) return
        event.isCancelled = true
    }

    @EventHandler
    private fun cancelShriek(event: GenericGameEvent) {
        if (!preventShriek) return
        if (event.event != GameEvent.SHRIEK) return
        val player = event.entity as? Player ?: return
        val user = player.user() ?: return
        if (!isActive(user)) return
        if (!user.isVanished) return
        event.isCancelled = true
    }

}