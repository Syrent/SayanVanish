package org.sayandev.sayanvanish.paper.feature.features.prevent

import com.destroystokyo.paper.event.entity.PreSpawnerSpawnEvent
import kotlinx.serialization.SerialName
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.api.feature.category.FeatureCategories
import org.sayandev.sayanvanish.paper.api.SayanVanishBukkitAPI.Companion.cachedVanishUser
import org.sayandev.sayanvanish.paper.feature.ListenedFeature
import org.sayandev.stickynote.bukkit.StickyNote
import org.sayandev.stickynote.bukkit.onlinePlayers
import org.sayandev.stickynote.bukkit.utils.ServerVersion
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@RegisteredFeature
@Serializable
@SerialName("prevent_spawner_spawn")
class FeaturePreventSpawnerSpawn: ListenedFeature() {

    @Transient override val id = "prevent_spawner_spawn"
    override var enabled: Boolean = true
    @Transient override val category: FeatureCategories = FeatureCategories.PREVENTION

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
        val allIsVanished = nearPlayers.all { it.cachedVanishUser()?.isVanished == true }
        if (allIsVanished) {
            event.isCancelled = true
        }
    }

}