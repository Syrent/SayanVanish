package ir.syrent.velocityvanish.spigot.listener

import ir.syrent.velocityvanish.spigot.VelocityVanishSpigot
import ir.syrent.velocityvanish.spigot.ruom.Ruom
import ir.syrent.velocityvanish.spigot.storage.Message
import ir.syrent.velocityvanish.spigot.storage.Settings
import ir.syrent.velocityvanish.spigot.utils.Utils
import ir.syrent.velocityvanish.spigot.utils.sendMessage
import ir.syrent.velocityvanish.utils.TextReplacement
import ir.syrent.velocityvanish.utils.component
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault

class PlayerJoinListener(
    private val plugin: VelocityVanishSpigot
) : Listener {

    init {
        Ruom.registerListener(this)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onPlayerJoin(event: PlayerJoinEvent) {
        handleVanishOnJoin(event)
    }

    fun handleVanishOnJoin(event: PlayerJoinEvent) {
        val player = event.player

        // Note: DiscordSRV support
        player.setMetadata("vanished", FixedMetadataValue(plugin, true))

        for (vanishedPlayer in plugin.vanishedNames.mapNotNull { Bukkit.getPlayerExact(it) }) {
            plugin.vanishManager.hidePlayer(vanishedPlayer)
            plugin.vanishManager.updateTabState(vanishedPlayer, GameMode.SPECTATOR)
            Ruom.runSync({
                plugin.vanishManager.updateTabState(vanishedPlayer, GameMode.SPECTATOR)
            }, 1)
        }

        if (plugin.vanishedNames.contains(player.name)) {
            if (!Settings.remember) return
            plugin.vanishManager.vanish(player, sendQuitMessage = false, callPostEvent = true)
            event.joinMessage = null
        } else {
            if (player.hasPermission(Permission("velocityvanish.action.vanish.onjoin", PermissionDefault.FALSE))) {
                plugin.vanishManager.vanish(player, sendQuitMessage = false, callPostEvent = true)
                event.joinMessage = null
            } else if (player.hasPermission("velocityvanish.action.vanish.force") && Settings.forceVanishIfFirst && Ruom.onlinePlayers.size <= 1) {
                if (!Settings.remember) return
                player.sendMessage(Message.FORCE_VANISHED)
                plugin.vanishManager.vanish(player, sendQuitMessage = false, callPostEvent = true, sendPluginMessage = false)
                event.joinMessage = null

                Ruom.runSync({
                    if (!plugin.vanishedNames.contains(player.name)) {
                        plugin.vanishManager.unVanish(player, sendJoinMessage = true, callPostEvent = true)
                    }
                }, 40)
            } else {
                plugin.vanishManager.unVanish(player, sendJoinMessage = false, callPostEvent = false)
            }
        }
    }

}