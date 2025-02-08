package org.sayandev.sayanvanish.bukkit.feature.features

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.sayandev.sayanvanish.api.feature.Configurable
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.user
import org.sayandev.sayanvanish.bukkit.feature.ListenedFeature
import org.sayandev.stickynote.bukkit.plugin
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import java.util.*

@RegisteredFeature
@ConfigSerializable
data class FeatureInventoryInspect(
    override var enabled: Boolean = true,
    @Configurable val modificationPermission: String = "${plugin.name}.features.inventory_inspect.modify"
): ListenedFeature("inventory_inspect") {

    val playerInventoryMap = mutableListOf<UUID>()

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onInteractPlayer(event: PlayerInteractAtEntityEvent) {
        val player = event.player
        val user = player.user() ?: return
        if (!isActive(user)) return
        if (player.openInventory.type != InventoryType.CRAFTING) return
        if (!user.isVanished) return
        val target = event.rightClicked as? Player ?: return

        player.openInventory(target.inventory)
        playerInventoryMap.add(player.uniqueId)
    }

    @EventHandler
    private fun onClickPlayerInventory(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val user = player.user() ?: return
        if (!isActive(user)) return
        if (!playerInventoryMap.contains(player.uniqueId)) return
        if (player.hasPermission(modificationPermission)) return
        event.isCancelled = true
    }

    @EventHandler
    private fun onInventoryClose(event: InventoryCloseEvent) {
        val player = event.player as? Player ?: return
        playerInventoryMap.remove(player.uniqueId)
    }




}