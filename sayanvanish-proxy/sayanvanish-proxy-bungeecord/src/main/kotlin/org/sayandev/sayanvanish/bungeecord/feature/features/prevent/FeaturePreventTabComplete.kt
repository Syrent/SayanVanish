package org.sayandev.sayanvanish.bungeecord.feature.features.prevent

import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.event.TabCompleteEvent
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority
import org.sayandev.sayanvanish.api.Permission
import org.sayandev.sayanvanish.api.VanishAPI
import org.sayandev.sayanvanish.api.VanishUser
import org.sayandev.sayanvanish.api.feature.Configurable
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.api.feature.category.FeatureCategories
import org.sayandev.sayanvanish.bungeecord.api.BungeeUser.Companion.generateAndSaveUser
import org.sayandev.sayanvanish.bungeecord.api.SayanVanishBungeeAPI
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
        val user = VanishAPI.get().getDatabase().getUserCache(player.uniqueId) ?: player.generateAndSaveUser()
        if (!isActive(user)) return
        val vanishUsers = SayanVanishBungeeAPI.getDatabase().getCachedVanishUsers().values
        if (!user.hasPermission(Permission.VANISH) || !checkVanishLevel) {
            event.suggestions
                .removeIf { suggestion -> vanishUsers.map(VanishUser::username).contains(suggestion) }
            return
        }

        val vanishUser = vanishUsers.firstOrNull { it.uniqueId == user.uniqueId }
        
        event.suggestions.removeIf { suggestion ->
            vanishUsers
                .filter { otherVanishUser -> otherVanishUser.vanishLevel > (vanishUser?.vanishLevel ?: -1) }
                .map(VanishUser::username).contains(suggestion)
        }
    }

}