package org.sayandev.sayanvanish.bukkit.feature.features.prevent

import kotlinx.serialization.SerialName
import org.bukkit.event.EventHandler
import org.bukkit.event.raid.RaidTriggerEvent
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.api.feature.category.FeatureCategories
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.cachedVanishUser
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.user
import org.sayandev.sayanvanish.bukkit.feature.ListenedFeature
import org.sayandev.stickynote.bukkit.event.registerListener
import org.sayandev.stickynote.bukkit.utils.ServerVersion
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@RegisteredFeature
@Serializable
@SerialName("prevent_raid_trigger")
class FeaturePreventRaidTrigger: ListenedFeature() {

    @Transient override val id = "prevent_raid_trigger"
    override var enabled: Boolean = true
    @Transient override val category: FeatureCategories = FeatureCategories.PREVENTION

    @Transient
    override var condition: Boolean = ServerVersion.supports(15)

    @EventHandler
    private fun onRaidTrigger(event: RaidTriggerEvent) {
        val user = event.player.cachedVanishUser() ?: return
        if (!isActive(user)) return
        if (user.isVanished) {
            event.isCancelled = true
        }
    }

}