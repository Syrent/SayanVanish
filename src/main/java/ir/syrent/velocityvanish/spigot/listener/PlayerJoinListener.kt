package ir.syrent.velocityvanish.spigot.listener

import ir.syrent.velocityvanish.spigot.VelocityVanishSpigot
import ir.syrent.velocityvanish.spigot.ruom.Ruom
import ir.syrent.velocityvanish.spigot.storage.Message
import ir.syrent.velocityvanish.spigot.storage.Settings
import ir.syrent.velocityvanish.spigot.utils.Utils
import ir.syrent.velocityvanish.utils.TextReplacement
import ir.syrent.velocityvanish.utils.component
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.metadata.FixedMetadataValue

class PlayerJoinListener(
    private val plugin: VelocityVanishSpigot
) : Listener {

    init {
        Ruom.registerListener(this)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onPlayerJoin(event: PlayerJoinEvent) {
        handleVanishOnJoin(event)
        Ruom.runSync({
            handleVanishOnJoin(event)
        }, 15)
    }

    fun handleVanishOnJoin(event: PlayerJoinEvent) {
        val oldPlayer = event.player
        /*
        * Velocity plugin message problem on 1.20.2 (1.20.2 is not yet supported offically)
        * https://github.com/PaperMC/Velocity/pull/1088#issuecomment-1744385241
        * */
        val player = Bukkit.getPlayer(oldPlayer.uniqueId) ?: return

        // Note: DiscordSRV support
        player.setMetadata("vanished", FixedMetadataValue(plugin, true))

        for (vanishedPlayer in plugin.vanishedNames.mapNotNull { Bukkit.getPlayerExact(it) }) {
            plugin.vanishManager.hidePlayer(vanishedPlayer)
            plugin.vanishManager.updateTabState(vanishedPlayer, GameMode.SPECTATOR)
            Ruom.runSync({
                plugin.vanishManager.updateTabState(vanishedPlayer, GameMode.SPECTATOR)
            }, 1)
        }

        if (!Settings.remember) return

        if (plugin.vanishedNames.contains(player.name)) {
            plugin.vanishManager.vanish(player, sendQuitMessage = false, callPostEvent = true)
            event.joinMessage = null
        } else {
            if (player.hasPermission("velocityvanish.action.vanish.onjoin")) {
                Ruom.runSync({
                    plugin.vanishManager.vanish(player, sendQuitMessage = false, callPostEvent = true)
                }, 1)
                event.joinMessage = null
            } else if (player.hasPermission("velocityvanish.action.vanish.force") && Settings.forceVanishIfFirst && Ruom.onlinePlayers.size <= 1) {
                plugin.vanishManager.vanish(player, sendQuitMessage = false, callPostEvent = true)
                event.joinMessage = null

                Ruom.runSync({
                    if (!plugin.vanishedNames.contains(player.name)) {
                        plugin.vanishManager.unVanish(player, sendJoinMessage = false, callPostEvent = true)
                    }

                    val joinMessage = Utils.getSerializedMessage(Settings.formatMessage(player, Message.JOIN_MESSAGE, TextReplacement("player", player.name), TextReplacement("play_displayname", player.displayName)))
                    if (joinMessage.isNotBlank() && joinMessage.isNotEmpty()) {
                        Ruom.broadcast(joinMessage.component())
                    }
                }, 40)
            } else {
                plugin.vanishManager.unVanish(player, sendJoinMessage = false, callPostEvent = false)
            }
        }
    }

}