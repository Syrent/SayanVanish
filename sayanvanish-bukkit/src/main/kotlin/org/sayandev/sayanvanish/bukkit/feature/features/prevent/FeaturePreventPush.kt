package org.sayandev.sayanvanish.bukkit.feature.features.prevent

import com.charleskorn.kaml.YamlComment
import kotlinx.serialization.SerialName
import org.bukkit.event.EventHandler
import org.bukkit.scoreboard.Team
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.api.feature.category.FeatureCategories
import org.sayandev.sayanvanish.bukkit.api.event.BukkitUserUnVanishEvent
import org.sayandev.sayanvanish.bukkit.api.event.BukkitUserVanishEvent
import org.sayandev.sayanvanish.bukkit.feature.ListenedFeature
import org.sayandev.stickynote.bukkit.StickyNote
import org.sayandev.stickynote.bukkit.hasPlugin
import org.sayandev.stickynote.bukkit.warn
import kotlinx.serialization.Serializable

@RegisteredFeature
@Serializable
@SerialName("prevent_push")
class FeaturePreventPush(
    @YamlComment("This feature might cause compatibility issues with eGlow plugin. If you use eGlow, disable this feature.")
    override var enabled: Boolean = false,
): ListenedFeature("prevent_push", enabled, category = FeatureCategories.PREVENTION) {

    @EventHandler
    private fun onVanish(event: BukkitUserVanishEvent) {
        val user = event.user
        if (!isActive(user)) return
        val player = user.player() ?: return


        if (hasPlugin("eGlow")) {
            StickyNote.warn("tried to register vanished team for user ${user.username} but $id feature is not compatible with eGlow. disable $id feature to remove the warning.")
            return
        }

        var team = player.scoreboard.getTeam("Vanished")
        if (team == null) {
            team = player.scoreboard.registerNewTeam("Vanished")
        }
        team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER)
        team.addEntry(player.name)
    }

    @EventHandler
    private fun onUnVanish(event: BukkitUserUnVanishEvent) {
        val user = event.user
        if (!isActive(user)) return
        val player = user.player() ?: return
        /* Make sure the player has `Vanished` team before removing it. Prevents 1.21 players to get kicked with ISE:
        java.lang.IllegalStateException: Player is either on another team or not on any team. Cannot remove from team 'Vanished'.*/
        val teams = player.scoreboard.teams
        if (teams.find { it.name == "Vanished" } == null) return

        if (hasPlugin("eGlow")) {
            StickyNote.warn("tried to remove vanished team for user ${user.username} but $id feature is not compatible with eGlow. disable $id feature to remove the warning.")
            return
        }

        player.scoreboard.getTeam("Vanished")?.removeEntry(player.name)
    }

}