package org.sayandev.sayanvanish.bukkit.feature.features.prevent

import org.bukkit.entity.Creature
import org.bukkit.event.EventHandler
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.api.feature.category.FeatureCategories
import org.sayandev.sayanvanish.bukkit.api.event.BukkitUserVanishEvent
import org.sayandev.sayanvanish.bukkit.feature.ListenedFeature
import org.sayandev.stickynote.lib.spongepowered.configurate.objectmapping.ConfigSerializable

@RegisteredFeature
@ConfigSerializable
class FeaturePreventCreatureTarget: ListenedFeature("prevent_creature_target", category = FeatureCategories.PREVENTION) {

    @EventHandler
    private fun onVanish(event: BukkitUserVanishEvent) {
        if (!isActive()) return
        val user = event.user
        val player = user.player() ?: return
        player.world.entities.stream()
            .filter { entity -> entity is Creature }
            .map { entity -> entity as Creature }
            .filter { mob -> mob.target != null }
            .filter { mob -> player.uniqueId == mob.target?.uniqueId }
            .forEach { mob -> mob.target = null }
    }

}