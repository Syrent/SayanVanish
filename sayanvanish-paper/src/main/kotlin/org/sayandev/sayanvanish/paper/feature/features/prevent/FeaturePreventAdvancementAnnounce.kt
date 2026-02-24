package org.sayandev.sayanvanish.paper.feature.features.prevent

import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerAdvancementDoneEvent
import org.sayandev.sayanvanish.api.feature.Configurable
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.api.feature.category.FeatureCategories
import org.sayandev.sayanvanish.paper.api.SayanVanishBukkitAPI.Companion.cachedVanishUser
import org.sayandev.sayanvanish.paper.feature.ListenedFeature
import org.sayandev.stickynote.bukkit.StickyNote
import org.sayandev.stickynote.bukkit.utils.ServerVersion
import kotlinx.serialization.Serializable
import com.charleskorn.kaml.YamlComment
import kotlinx.serialization.SerialName
import kotlinx.serialization.Transient

@RegisteredFeature
@Serializable
@SerialName("prevent_advancement_announce")
class FeaturePreventAdvancementAnnounce(
    @YamlComment("Whether to disable the advancement announce message when the player is vanished.")
    @Configurable val disableMessage: Boolean = true,
    @YamlComment("Whether to revoke the criteria when the player is vanished.")
    @Configurable val revokeCriteria: Boolean = false
): ListenedFeature() {

    @Transient override val id = "prevent_advancement_announce"
    override var enabled: Boolean = true
    @Transient override val category: FeatureCategories = FeatureCategories.PREVENTION

    @Transient
    override var condition: Boolean = StickyNote.isPaper && ServerVersion.supports(13)

    @EventHandler
    private fun onAdvancementDone(event: PlayerAdvancementDoneEvent) {
        val user = event.player.cachedVanishUser() ?: return
        if (!isActive(user)) return
        if (user.isVanished) {
            if (disableMessage) {
                event.message(null)
            }
            if (revokeCriteria) {
                for (criteria in event.advancement.criteria) {
                    event.player.getAdvancementProgress(event.advancement).revokeCriteria(criteria)
                }
            }
        }
    }

}