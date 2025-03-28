package org.sayandev.sayanvanish.bukkit.feature.features

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.user
import org.sayandev.sayanvanish.bukkit.feature.ListenedFeature
import org.sayandev.sayanvanish.bukkit.utils.PlayerUtils.sendComponent
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@RegisteredFeature
@ConfigSerializable
class FeatureRideEntity(
    val leaveVehicleWhenOthersEnter: Boolean = true,
    val exitMessage: String = "<red>You have been removed from the vehicle. because someone else entered it.",
): ListenedFeature("ride_entity") {

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun removeVanishedPassengerOnEnter(event: PlayerInteractEntityEvent) {
        if (!leaveVehicleWhenOthersEnter) return
        val targetEntity = event.rightClicked
        val vanishedPassengers = targetEntity.passengers
            .filterIsInstance<Player>()
            .mapNotNull { player -> player.user() }
            .filter { it.isVanished }
            .mapNotNull { it.player() }
        for (vanishedPassenger in vanishedPassengers) {
            vanishedPassenger.leaveVehicle()
            vanishedPassenger.sendComponent(exitMessage)
        }
    }

}