package org.sayandev.sayanvanish.bukkit.feature.features.prevent

import com.destroystokyo.paper.event.entity.PreSpawnerSpawnEvent
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.api.feature.category.FeatureCategories
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.user
import org.sayandev.sayanvanish.bukkit.feature.ListenedFeature
import org.sayandev.stickynote.bukkit.StickyNote
import org.sayandev.stickynote.bukkit.onlinePlayers
import org.sayandev.stickynote.lib.spongepowered.configurate.objectmapping.ConfigSerializable
import org.sayandev.stickynote.lib.xseries.ReflectionUtils
import kotlin.collections.all
import kotlin.collections.filter

@RegisteredFeature
@ConfigSerializable
class FeaturePreventSpawnerSpawn: ListenedFeature("prevent_spawner_spawn", category = FeatureCategories.PREVENTION) {

    @Transient
    override var condition: Boolean = StickyNote.isPaper() && ReflectionUtils.supports(16)

    @EventHandler
    private fun onPreSpawn(event: PreSpawnerSpawnEvent) {
        if (!isActive()) return
        val nearPlayers = onlinePlayers
            .filter { player -> player.world == event.spawnerLocation.world && player.location.distance(event.spawnerLocation) <= 256 && player.gameMode != GameMode.SPECTATOR }
        val allIsVanished = nearPlayers.all { it.user()?.isVanished == true }
        if (allIsVanished) {
            event.isCancelled = true
        }
    }

}