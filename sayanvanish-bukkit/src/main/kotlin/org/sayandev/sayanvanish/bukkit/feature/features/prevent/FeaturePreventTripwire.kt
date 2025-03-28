package org.sayandev.sayanvanish.bukkit.feature.features.prevent

import com.cryptomorin.xseries.XMaterial
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.api.feature.category.FeatureCategories
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.user
import org.sayandev.sayanvanish.bukkit.feature.ListenedFeature
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@RegisteredFeature
@ConfigSerializable
class FeaturePreventTripwire: ListenedFeature("prevent_tripwire", category = FeatureCategories.PREVENTION) {

    @EventHandler
    private fun cancelTripwireInteract(event: PlayerInteractEvent) {
        val block = event.clickedBlock ?: return
        if (event.action != Action.PHYSICAL) return
        if (block.type != XMaterial.TRIPWIRE.get()!! && block.type != XMaterial.STRING.get()!!) return
        val player = event.player
        val user = player.user() ?: return
        if (!isActive(user)) return
        if (!user.isVanished) return
        event.isCancelled = true
    }

}