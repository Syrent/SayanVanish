package org.sayandev.sayanvanish.bukkit.feature.features.prevent

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.server.TabCompleteEvent
import org.sayandev.sayanvanish.api.Permission
import org.sayandev.sayanvanish.api.User
import org.sayandev.sayanvanish.api.feature.Configurable
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.api.feature.category.FeatureCategories
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.getOrCreateUser
import org.sayandev.sayanvanish.bukkit.feature.ListenedFeature
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment

@RegisteredFeature
@ConfigSerializable
data class FeaturePreventTabComplete(
    @Comment("Whether to keep vanished player in tab completion if the player that is getting the suggestion has a higher level of vanish.")
    @Configurable val checkVanishLevel: Boolean = false
): ListenedFeature("prevent_tab_complete", category = FeatureCategories.PREVENTION) {

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onTabComplete(event: TabCompleteEvent) {
        val player = event.sender as? Player ?: return
        val user = player.getOrCreateUser()
        if (!isActive(user)) return
        val vanishedUsers = SayanVanishBukkitAPI.getInstance().getVanishedUsers()
        val completions = event.completions.toMutableSet()
        if (!user.hasPermission(Permission.VANISH) || !checkVanishLevel) {
            event.completions = completions
                .filter { completion -> !vanishedUsers.map(User::username).contains(completion) }
            return
        }

        event.completions = completions.filter { completion ->
            !vanishedUsers
                .filter { vanishedUser -> vanishedUser.vanishLevel > user.vanishLevel }
                .map(User::username).contains(completion)
        }
    }

}