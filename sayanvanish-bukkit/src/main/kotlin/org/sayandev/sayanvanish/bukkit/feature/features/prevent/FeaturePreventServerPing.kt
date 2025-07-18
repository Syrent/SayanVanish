package org.sayandev.sayanvanish.bukkit.feature.features.prevent

import com.destroystokyo.paper.event.server.PaperServerListPingEvent
import org.bukkit.event.EventHandler
import org.sayandev.sayanvanish.api.VanishAPI
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.api.feature.category.FeatureCategories
import org.sayandev.sayanvanish.bukkit.api.BukkitVanishUser.Companion.bukkitAdapt
import org.sayandev.sayanvanish.bukkit.feature.ListenedFeature
import org.sayandev.stickynote.bukkit.StickyNote
import org.sayandev.stickynote.bukkit.utils.ServerVersion
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@RegisteredFeature
@ConfigSerializable
class FeaturePreventServerPing: ListenedFeature("prevent_server_ping", category = FeatureCategories.PREVENTION) {

    @EventHandler
    private fun onPing(event: PaperServerListPingEvent) {
        if (!isActive()) return
        val vanishedPlayers = VanishAPI.get().getCacheService().getVanishUsers().getVanished().filter { it.bukkitAdapt().player() != null }
        event.numPlayers -= vanishedPlayers.count()
        if (StickyNote.isPaper && ServerVersion.supports(21)) {
            event.listedPlayers.removeIf { profile -> vanishedPlayers.map { vanishedPlayer -> vanishedPlayer.uniqueId }.contains(profile.id) }
        } else {
            event.playerSample.removeIf { profile -> vanishedPlayers.map { vanishedPlayer -> vanishedPlayer.uniqueId }.contains(profile.id) }
        }
    }

}