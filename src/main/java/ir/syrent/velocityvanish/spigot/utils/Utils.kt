package ir.syrent.velocityvanish.spigot.utils

import ir.syrent.velocityvanish.spigot.VelocityVanishSpigot
import ir.syrent.velocityvanish.spigot.ruom.Ruom
import ir.syrent.velocityvanish.spigot.ruom.string.CharAnimation
import ir.syrent.velocityvanish.spigot.storage.Message
import ir.syrent.velocityvanish.spigot.storage.Settings
import ir.syrent.velocityvanish.utils.TextReplacement
import org.bukkit.entity.Player

object Utils {

    val actionbarPlayers = mutableSetOf<Player>()

    fun sendReportsActionbar(player: Player) {
        if (actionbarPlayers.contains(player)) return

        val charAnimation = CharAnimation(CharAnimation.Style.SQUARE_BLOCK)

        if (Settings.actionbar && player.hasPermission("velocityvanish.admin.actionbar")) {
            Ruom.runSync({
                if (!VelocityVanishSpigot.instance.vanishedNames.contains(player.name)) return@runSync
                player.sendActionbar(Message.VANISH_ACTIONBAR, TextReplacement("animation", charAnimation.get().toString()))
                actionbarPlayers.add(player)
            }, 0, 20)
        }
    }

}