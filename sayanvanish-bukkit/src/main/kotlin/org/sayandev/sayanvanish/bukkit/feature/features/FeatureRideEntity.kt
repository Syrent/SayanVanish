package org.sayandev.sayanvanish.bukkit.feature.features

import kotlinx.serialization.SerialName
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.bukkit.api.BukkitVanishUser.Companion.bukkitAdapt
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.cachedVanishUser
import org.sayandev.sayanvanish.bukkit.feature.ListenedFeature
import org.sayandev.sayanvanish.bukkit.utils.PlayerUtils.sendPrefixComponent
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@RegisteredFeature
@Serializable
@SerialName("ride_entity")
class FeatureRideEntity(
    val leaveVehicleWhenOthersEnter: Boolean = true,
    val exitMessage: String = "<red>You have been removed from the vehicle. because someone else entered it.",
): ListenedFeature() {

    @Transient override val id = "ride_entity"
    override var enabled: Boolean = true

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun removeVanishedPassengerOnEnter(event: PlayerInteractEntityEvent) {
        if (!leaveVehicleWhenOthersEnter) return
        val targetEntity = event.rightClicked
        val vanishedPassengers = targetEntity.passengers
            .filterIsInstance<Player>()
            .mapNotNull { player -> player.cachedVanishUser() }
            .filter { it.isVanished }
            .mapNotNull { it.bukkitAdapt().player() }
        for (vanishedPassenger in vanishedPassengers) {
            vanishedPassenger.leaveVehicle()
            vanishedPassenger.sendPrefixComponent(exitMessage)
        }
    }

}