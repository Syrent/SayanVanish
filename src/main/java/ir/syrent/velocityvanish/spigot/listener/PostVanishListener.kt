package ir.syrent.velocityvanish.spigot.listener

import ir.syrent.velocityvanish.spigot.VelocityVanishSpigot
import ir.syrent.velocityvanish.spigot.event.PostVanishEvent
import ir.syrent.velocityvanish.spigot.hook.DependencyManager
import ir.syrent.velocityvanish.spigot.ruom.Ruom
import ir.syrent.velocityvanish.spigot.storage.Message
import ir.syrent.velocityvanish.spigot.storage.Settings
import ir.syrent.velocityvanish.spigot.utils.Utils
import ir.syrent.velocityvanish.utils.TextReplacement
import ir.syrent.velocityvanish.utils.component
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class PostVanishListener(
    private val plugin: VelocityVanishSpigot
) : Listener {

    init {
        Ruom.registerListener(this)
    }

    @EventHandler
    private fun onPostVanish(event: PostVanishEvent) {
        val player = event.player
        Utils.sendReportsActionbar(player)
        plugin.vanishedNames.add(player.name)
        plugin.bridgeManager?.updateVanishedPlayersRequest(player, true)

        val quitMessage = Settings.formatMessage(Message.QUIT_MESSAGE, TextReplacement("player", player.name), TextReplacement("play_displayname", player.displayName))
        if (quitMessage.isNotBlank() && quitMessage.isNotEmpty() && event.sendQuitMessage) {
            Ruom.broadcast(quitMessage.component())
        }

        if (DependencyManager.proCosmeticsHook.exists) {
            DependencyManager.proCosmeticsHook.proCosmetics.userManager.getUser(player.uniqueId).unequipCosmetics(true)
        }
    }
}