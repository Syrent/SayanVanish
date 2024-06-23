package org.sayandev.sayanvanish.bukkit.feature.features.prevent

import org.bukkit.Bukkit
import org.bukkit.entity.Creature
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityTargetEvent
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.api.feature.category.FeatureCategories
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI
import org.sayandev.sayanvanish.bukkit.api.event.BukkitUserVanishEvent
import org.sayandev.sayanvanish.bukkit.feature.ListenedFeature
import org.sayandev.stickynote.bukkit.StickyNote
import org.sayandev.stickynote.bukkit.WrappedStickyNotePlugin
import org.sayandev.stickynote.lib.spongepowered.configurate.objectmapping.ConfigSerializable

@RegisteredFeature
@ConfigSerializable
class FeaturePreventCreatureTarget: ListenedFeature("prevent_creature_target", category = FeatureCategories.PREVENTION) {

    @EventHandler
    private fun onVanish(event: BukkitUserVanishEvent) {
        if (!isActive()) return
        val user = event.user
        val player = user.player() ?: return
        if (StickyNote.isFolia()) {
            player.server.regionScheduler.execute(WrappedStickyNotePlugin.getPlugin().main, player.location) {
                val entity = Bukkit.getEntity(player.uniqueId)
                if (entity is Creature) {
                    entity.target = null
                }
            }
        } else {
            player.world.entities
                .filterIsInstance<Creature>()
                .filter { mob -> player.uniqueId == mob.target?.uniqueId }
                .forEach { mob -> mob.target = null }
        }
    }

    @EventHandler
    private fun onEntityTarget(event: EntityTargetEvent) {
        if (!isActive()) return
        val target = event.target as? Player ?: return
        if (!SayanVanishBukkitAPI.getInstance().isVanished(target.uniqueId)) return
        event.isCancelled = true
    }

}