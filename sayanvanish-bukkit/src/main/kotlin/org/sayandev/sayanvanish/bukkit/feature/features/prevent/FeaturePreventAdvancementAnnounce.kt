package org.sayandev.sayanvanish.bukkit.feature.features.prevent

import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerAdvancementDoneEvent
import org.sayandev.sayanvanish.api.feature.Configurable
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.api.feature.category.FeatureCategories
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.user
import org.sayandev.sayanvanish.bukkit.feature.ListenedFeature
import org.sayandev.stickynote.bukkit.StickyNote
import org.sayandev.stickynote.bukkit.utils.ServerVersion
import org.sayandev.stickynote.lib.spongepowered.configurate.objectmapping.ConfigSerializable

@RegisteredFeature
@ConfigSerializable
class FeaturePreventAdvancementAnnounce(
    @Configurable val disableMessage: Boolean = true,
    @Configurable val revokeCriteria: Boolean = false
): ListenedFeature("prevent_advancement_announce", category = FeatureCategories.PREVENTION) {

    @Transient
    override var condition: Boolean = StickyNote.isPaper() && ServerVersion.supports(13)

    @EventHandler
    private fun onAdvancementDone(event: PlayerAdvancementDoneEvent) {
        if (!isActive()) return
        val user = event.player.user() ?: return
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