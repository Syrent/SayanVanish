package org.sayandev.sayanvanish.bukkit.feature.features.prevent

import org.bukkit.entity.Mob
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityTargetEvent
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.api.feature.category.FeatureCategories
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.cachedVanishUser
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.user
import org.sayandev.sayanvanish.bukkit.api.event.BukkitUserVanishEvent
import org.sayandev.sayanvanish.bukkit.feature.ListenedFeature
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@RegisteredFeature
@ConfigSerializable
class FeaturePreventCreatureTarget: ListenedFeature("prevent_creature_target", category = FeatureCategories.PREVENTION) {

    @EventHandler
    private fun preventEntityTargetOnVanish(event: BukkitUserVanishEvent) {
        val user = event.user
        val player = user.player() ?: return
        for (entity in player.world.entities.filterIsInstance<Mob>()) {
            if (entity.target != player) continue
            entity.target = null
        }
    }

    @EventHandler
    private fun onEntityTarget(event: EntityTargetEvent) {
        val target = event.target as? Player ?: return
        val user = target.cachedVanishUser() ?: return
        if (!isActive(user)) return
        if (!user.isVanished) return
        event.isCancelled = true
    }

}