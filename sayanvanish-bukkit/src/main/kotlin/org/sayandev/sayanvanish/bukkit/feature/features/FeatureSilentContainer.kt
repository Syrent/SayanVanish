package org.sayandev.sayanvanish.bukkit.feature.features

import org.bukkit.GameMode
import org.bukkit.block.Container
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.inventory.CraftingInventory
import org.bukkit.inventory.PlayerInventory
import org.bukkit.util.Vector
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.user
import org.sayandev.sayanvanish.bukkit.api.event.BukkitUserUnVanishEvent
import org.sayandev.sayanvanish.bukkit.feature.ListenedFeature
import org.sayandev.stickynote.bukkit.runSync
import org.sayandev.stickynote.bukkit.utils.ServerVersion
import org.sayandev.stickynote.bukkit.warn
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import java.util.*

@RegisteredFeature
@ConfigSerializable
class FeatureSilentContainer: ListenedFeature("silent_container") {

    override var condition: Boolean = ServerVersion.supports(13)
    @Transient private val containerPlayersData = mutableMapOf<UUID, ContainerPlayerData>()

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = event.player
        val user = player.user() ?: return
        if (!isActive(user)) return
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        val clickedBlock = event.clickedBlock ?: return
        if (clickedBlock.state !is Container) return
        if (!user.isVanished) return
        if (player.gameMode == GameMode.SPECTATOR) return

        containerPlayersData[player.uniqueId] = ContainerPlayerData(player.gameMode, player.allowFlight, player.isFlying)

        player.allowFlight = true
        player.isFlying = true
        player.velocity = Vector(0.0, 0.0, 0.0)
        player.gameMode = GameMode.SPECTATOR

        runSync({
            containerPlayersData[player.uniqueId]?.apply(player)
            containerPlayersData.remove(player.uniqueId)
        }, 1)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onTeleport(event: PlayerTeleportEvent) {
        val player = event.player
        // Allow spectators to teleport via their hotbar
        if (event.cause == PlayerTeleportEvent.TeleportCause.SPECTATE) return
        if (!containerPlayersData.contains(player.uniqueId) && event.cause != PlayerTeleportEvent.TeleportCause.SPECTATE) return
        event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private fun onUnVanish(event: BukkitUserUnVanishEvent) {
        val player = event.user.player()
        if (player != null) {
            containerPlayersData[player.uniqueId]?.apply(player)
        }

        containerPlayersData.remove(event.user.uniqueId)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onQuit(event: PlayerQuitEvent) {
        containerPlayersData[event.player.uniqueId]?.apply(event.player)
        containerPlayersData.remove(event.player.uniqueId)
    }

    @EventHandler
    private fun onInventoryClose(event: InventoryCloseEvent) {
        val player = event.player as? Player ?: return
        val user = player.user() ?: return
        if (!user.isVanished) return
        if (event.inventory is CraftingInventory) return

        containerPlayersData[player.uniqueId] = ContainerPlayerData(player.gameMode, player.allowFlight, player.isFlying)

        player.allowFlight = true
        player.isFlying = true
        player.velocity = Vector(0.0, 0.0, 0.0)
        player.gameMode = GameMode.SPECTATOR

        runSync({
            containerPlayersData[player.uniqueId]?.apply(player)
            containerPlayersData.remove(player.uniqueId)
        }, 1)
    }

    data class ContainerPlayerData(
        val gameMode: GameMode,
        val allowFlight: Boolean,
        val isFlying: Boolean,
    ) {
        fun apply(player: Player) {
            player.gameMode = gameMode
            player.allowFlight = allowFlight
            player.isFlying = isFlying
        }
    }

}