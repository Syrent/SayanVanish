package org.sayandev.sayanvanish.bukkit.feature.features.prevent

import com.destroystokyo.paper.event.server.PaperServerListPingEvent
import org.bukkit.event.EventHandler
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.api.feature.category.FeatureCategories
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI
import org.sayandev.sayanvanish.bukkit.feature.ListenedFeature
import org.sayandev.stickynote.bukkit.StickyNote
import org.sayandev.stickynote.bukkit.utils.ServerVersion
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import kotlin.collections.contains
import kotlin.collections.count
import kotlin.collections.map

@RegisteredFeature
@ConfigSerializable
class FeaturePreventServerPing: ListenedFeature("prevent_server_ping", category = FeatureCategories.PREVENTION) {

    @Transient
    override var condition: Boolean = StickyNote.isPaper() && ServerVersion.supports(16)

    @EventHandler
    private fun onPreSpawn(event: PaperServerListPingEvent) {
        if (!isActive()) return
        val vanishedPlayers = SayanVanishBukkitAPI.getInstance().database.getUsers().filter { it.player() != null }
        event.numPlayers -= vanishedPlayers.count()
        event.playerSample.removeIf { profile -> vanishedPlayers.map { vanishedPlayer -> vanishedPlayer.uniqueId }.contains(profile.id) }
    }

}