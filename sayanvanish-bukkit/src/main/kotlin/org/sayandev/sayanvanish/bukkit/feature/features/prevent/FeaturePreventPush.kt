package org.sayandev.sayanvanish.bukkit.feature.features.prevent

import org.bukkit.event.EventHandler
import org.bukkit.scoreboard.Team
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.api.feature.category.FeatureCategories
import org.sayandev.sayanvanish.bukkit.api.event.BukkitUserUnVanishEvent
import org.sayandev.sayanvanish.bukkit.api.event.BukkitUserVanishEvent
import org.sayandev.sayanvanish.bukkit.feature.ListenedFeature
import org.sayandev.stickynote.lib.spongepowered.configurate.objectmapping.ConfigSerializable

@RegisteredFeature
@ConfigSerializable
class FeaturePreventPush: ListenedFeature("prevent_push", enabled = false, category = FeatureCategories.PREVENTION) {

    @EventHandler
    private fun onVanish(event: BukkitUserVanishEvent) {
        if (!isActive()) return
        val user = event.user
        val player = user.player() ?: return
        var team = player.scoreboard.getTeam("Vanished")
        if (team == null) {
            team = player.scoreboard.registerNewTeam("Vanished")
        }
        team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER)
        team.addEntry(player.name)
    }

    @EventHandler
    private fun onUnVanish(event: BukkitUserUnVanishEvent) {
        if (!isActive()) return
        val user = event.user
        val player = user.player() ?: return
        player.scoreboard.getTeam("Vanished")?.removeEntry(player.name)
    }

}