package org.sayandev.sayanvanish.bukkit.feature.features.prevent

import org.bukkit.event.EventHandler
import org.bukkit.event.raid.RaidTriggerEvent
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.api.feature.category.FeatureCategories
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.user
import org.sayandev.sayanvanish.bukkit.feature.ListenedFeature
import org.sayandev.stickynote.bukkit.event.registerListener
import org.sayandev.stickynote.bukkit.utils.ServerVersion
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@RegisteredFeature
@ConfigSerializable
class FeaturePreventRaidTrigger: ListenedFeature("prevent_raid_trigger", category = FeatureCategories.PREVENTION) {

    @Transient
    override var condition: Boolean = ServerVersion.supports(15)

    @EventHandler
    private fun onRaidTrigger(event: RaidTriggerEvent) {
        if (!isActive()) return
        val user = event.player.user() ?: return
        if (user.isVanished) {
            event.isCancelled = true
        }
    }

}