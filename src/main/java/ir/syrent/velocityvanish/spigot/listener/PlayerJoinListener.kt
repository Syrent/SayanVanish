package ir.syrent.velocityvanish.spigot.listener

import github.scarsz.discordsrv.DiscordSRV
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
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PlayerJoinListener(
    private val plugin: VelocityVanishSpigot
) : Listener {

    init {
        Ruom.registerListener(this)
    }

    @EventHandler
    private fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player

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
            } else if (player.hasPermission("velocityvanish.action.vanish.force") && Settings.forceVanishIfFirst && Ruom.getOnlinePlayers().size <= 1) {
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