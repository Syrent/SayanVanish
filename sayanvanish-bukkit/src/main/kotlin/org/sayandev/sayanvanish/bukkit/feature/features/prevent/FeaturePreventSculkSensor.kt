package org.sayandev.sayanvanish.bukkit.feature.features.prevent

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockReceiveGameEvent
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.api.feature.category.FeatureCategories
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.user
import org.sayandev.sayanvanish.bukkit.feature.ListenedFeature
import org.sayandev.stickynote.lib.spongepowered.configurate.objectmapping.ConfigSerializable
import org.sayandev.stickynote.lib.xseries.ReflectionUtils

@RegisteredFeature
@ConfigSerializable
class FeaturePreventSculkSensor: ListenedFeature("prevent_sculk_sensor", category = FeatureCategories.PREVENTION) {

    @Transient
    override var condition: Boolean = ReflectionUtils.supports(19)

    @EventHandler
    private fun onBlockReceive(event: BlockReceiveGameEvent) {
        if (!isActive()) return
        val user = (event.entity as? Player)?.user() ?: return
        if (user.isVanished) {
            event.isCancelled = true
        }
    }

}