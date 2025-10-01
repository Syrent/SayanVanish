package org.sayandev.sayanvanish.velocity.feature.features.prevent

import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.TabCompleteEvent
import org.sayandev.sayanvanish.api.Permission
import org.sayandev.sayanvanish.api.VanishUser
import org.sayandev.sayanvanish.api.feature.Configurable
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.api.feature.category.FeatureCategories
import org.sayandev.sayanvanish.velocity.api.SayanVanishVelocityAPI
import org.sayandev.sayanvanish.velocity.api.SayanVanishVelocityAPI.Companion.getOrCreateUser
import org.sayandev.sayanvanish.velocity.feature.ListenedFeature
import kotlinx.serialization.Serializable
import com.charleskorn.kaml.YamlComment
import kotlinx.serialization.SerialName
import kotlinx.serialization.Transient
import org.sayandev.sayanvanish.api.VanishAPI
import org.sayandev.sayanvanish.velocity.api.SayanVanishVelocityAPI.Companion.getCachedOrCreateVanishUser

@RegisteredFeature
@Serializable
@SerialName("prevent_tab_complete")
class FeaturePreventTabComplete(
    @YamlComment("Whether to keep vanished player in tab completion if the player that is getting the suggestion has a higher level of vanish.")
    @Configurable val checkVanishLevel: Boolean = false
): ListenedFeature() {

    @Transient override val id = "prevent_tab_complete"
    override val category = FeatureCategories.PREVENTION
    override var enabled: Boolean = true

    @Subscribe(order = PostOrder.LAST)
    fun onTabComplete(event: TabCompleteEvent) {
        val player = event.player ?: return
        val user = player.getCachedOrCreateVanishUser()
        if (!isActive(user)) return
        val vanishedUsers = VanishAPI.get().getCacheService().getVanishUsers().values.filter { it.isVanished }
        if (!user.hasPermission(Permission.VANISH) || !checkVanishLevel) {
            event.suggestions
                .removeIf { suggestion -> vanishedUsers.map(VanishUser::username).contains(suggestion) }
            return
        }
        
        event.suggestions.removeIf { suggestion ->
            vanishedUsers
                .filter { vanishedUser -> vanishedUser.vanishLevel > user.vanishLevel }
                .map(VanishUser::username).contains(suggestion)
        }
    }

}