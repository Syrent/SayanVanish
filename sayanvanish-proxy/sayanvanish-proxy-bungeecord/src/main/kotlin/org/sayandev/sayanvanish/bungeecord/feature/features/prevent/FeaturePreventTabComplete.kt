package org.sayandev.sayanvanish.bungeecord.feature.features.prevent

import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.event.TabCompleteEvent
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority
import org.sayandev.sayanvanish.api.Permission
import org.sayandev.sayanvanish.api.User
import org.sayandev.sayanvanish.api.feature.Configurable
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.api.feature.category.FeatureCategories
import org.sayandev.sayanvanish.bungeecord.api.SayanVanishBungeeAPI
import org.sayandev.sayanvanish.bungeecord.api.SayanVanishBungeeAPI.Companion.getOrCreateUser
import org.sayandev.sayanvanish.bungeecord.feature.ListenedFeature
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@RegisteredFeature
@ConfigSerializable
class FeaturePreventTabComplete(
    @Configurable val checkVanishLevel: Boolean = false
): ListenedFeature("prevent_tab_complete", category = FeatureCategories.PREVENTION) {

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onTabComplete(event: TabCompleteEvent) {
        val player = event.sender as? ProxiedPlayer ?: return
        val user = player.getOrCreateUser()
        if (!isActive(user)) return
        val vanishedUsers = SayanVanishBungeeAPI.getInstance().getVanishedUsers()
        if (!user.hasPermission(Permission.VANISH) || !checkVanishLevel) {
            event.suggestions
                .removeIf { suggestion -> vanishedUsers.map(User::username).contains(suggestion) }
            return
        }
        
        event.suggestions.removeIf { suggestion ->
            vanishedUsers
                .filter { vanishedUser -> vanishedUser.vanishLevel > user.vanishLevel }
                .map(User::username).contains(suggestion)
        }
    }

}