package org.sayandev.sayanvanish.bukkit.feature.features.prevent

import com.destroystokyo.paper.event.entity.PreSpawnerSpawnEvent
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.api.feature.category.FeatureCategories
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.user
import org.sayandev.sayanvanish.bukkit.feature.ListenedFeature
import org.sayandev.stickynote.bukkit.StickyNote
import org.sayandev.stickynote.bukkit.onlinePlayers
import org.sayandev.stickynote.bukkit.utils.ServerVersion
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@RegisteredFeature
@ConfigSerializable
class FeaturePreventSpawnerSpawn: ListenedFeature("prevent_spawner_spawn", category = FeatureCategories.PREVENTION) {

    @Transient
    override var condition: Boolean = StickyNote.isPaper && ServerVersion.supports(16)

    /**
    * @note This feature will be too performance intense if i want to check feature is active or not on a per-player basis
    * */
    @EventHandler
    private fun onPreSpawn(event: PreSpawnerSpawnEvent) {
        if (!isActive()) return
        val nearPlayers = onlinePlayers
            .filter { player -> player.gameMode != GameMode.SPECTATOR && player.world == event.spawnerLocation.world && player.location.distance(event.spawnerLocation) <= 256 }
        val allIsVanished = nearPlayers.all { it.user()?.isVanished == true }
        if (allIsVanished) {
            event.isCancelled = true
        }
    }

}