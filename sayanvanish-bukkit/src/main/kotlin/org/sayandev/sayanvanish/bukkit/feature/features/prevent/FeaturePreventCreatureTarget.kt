package org.sayandev.sayanvanish.bukkit.feature.features.prevent

import org.bukkit.entity.Creature
import org.bukkit.event.EventHandler
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.api.feature.category.FeatureCategories
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI
import org.sayandev.sayanvanish.bukkit.api.event.BukkitUserVanishEvent
import org.sayandev.sayanvanish.bukkit.feature.ListenedFeature
import org.sayandev.stickynote.bukkit.StickyNote
import org.sayandev.stickynote.bukkit.runSync
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
            // TODO: Creature modification cannot be off region thread
            player.world.entities
                .filterIsInstance<Creature>()
                .forEach { creature ->
                    if (creature.target?.uniqueId == player.uniqueId) {
                        creature.target = null
                    }
                }
        } else {
            player.world.entities
                .filterIsInstance<Creature>()
                .filter { mob -> player.uniqueId == mob.target?.uniqueId }
                .forEach { mob -> mob.target = null }
        }
    }

    override fun enable() {
        runSync({
            if (isActive()) return@runSync
            for (player in SayanVanishBukkitAPI.getInstance().getVanishedUsers().mapNotNull { it.player() }) {
                if (StickyNote.isFolia()) {
                    // TODO: Creature modification cannot be off region thread
                    player.world.entities
                        .filterIsInstance<Creature>()
                        .forEach { creature ->
                            if (creature.target?.uniqueId == player.uniqueId) {
                                creature.target = null
                            }
                        }
                } else {
                    player.world.entities
                        .filterIsInstance<Creature>()
                        .filter { mob -> player.uniqueId == mob.target?.uniqueId }
                        .forEach { mob -> mob.target = null }
                }
            }
        }, 20, 20)
        super.enable()
    }

}