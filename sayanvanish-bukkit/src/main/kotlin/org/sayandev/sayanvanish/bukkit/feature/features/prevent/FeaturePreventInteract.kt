package org.sayandev.sayanvanish.bukkit.feature.features.prevent

import org.bukkit.block.Container
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.sayandev.sayanvanish.api.feature.Configurable
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.api.feature.category.FeatureCategories
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.user
import org.sayandev.sayanvanish.bukkit.feature.ListenedFeature
import org.sayandev.stickynote.bukkit.utils.ServerVersion
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment

@RegisteredFeature
@ConfigSerializable
class FeaturePreventInteract(
    @Comment("Prevent players from activating pressure plates while vanished")
    @Configurable val pressurePlateTrigger: Boolean = true,
    @Comment("Prevent players from interacting with big dripleaf while vanished")
    @Configurable val dripLeaf: Boolean = true,
    @Comment("Prevent players from interacting")
    @Configurable val interact: Boolean = false,
) : ListenedFeature("prevent_interact_event", category = FeatureCategories.PREVENTION) {

    @EventHandler
    private fun onInteract(event: PlayerInteractEvent) {
        if (ServerVersion.supports(13)) {
            if (event.clickedBlock?.state is Container) return
        }
        val user = event.player.user() ?: return
        if (!isActive(user)) return
        if (user.isVanished) {
            val isPressurePlate = pressurePlateTrigger && event.action == Action.PHYSICAL && event.clickedBlock?.type?.name?.contains("PLATE") == true
            val isDripLeaf = dripLeaf && event.action == Action.PHYSICAL && event.clickedBlock?.type?.name?.equals("BIG_DRIPLEAF") == true
            if (interact || (isPressurePlate && pressurePlateTrigger) || (isDripLeaf && dripLeaf)) {
                event.isCancelled = true
            }
        }
    }

}