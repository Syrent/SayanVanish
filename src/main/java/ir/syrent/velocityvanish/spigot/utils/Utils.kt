package ir.syrent.velocityvanish.spigot.utils

import ir.syrent.velocityvanish.spigot.VelocityVanishSpigot
import ir.syrent.velocityvanish.spigot.ruom.Ruom
import ir.syrent.velocityvanish.spigot.ruom.string.CharAnimation
import ir.syrent.velocityvanish.spigot.storage.Message
import ir.syrent.velocityvanish.spigot.storage.Settings
import ir.syrent.velocityvanish.utils.TextReplacement
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

object Utils {

    // TODO: Crate new Actionbar class

    val actionbarPlayers = mutableSetOf<Player>()
    val charAnimation = CharAnimation(CharAnimation.Style.SQUARE_BLOCK)
    var lastChar = ""

    init {
        Ruom.runSync({
            lastChar = charAnimation.get().toString()
        }, 0, 30)
    }

    fun sendVanishActionbar(player: Player) {
        if (actionbarPlayers.contains(player)) return

        if (Settings.actionbar && player.hasPermission("velocityvanish.admin.actionbar")) {
            object : BukkitRunnable() {
                override fun run() {
                    if (Bukkit.getPlayer(player.uniqueId) == null) {
                        cancel()
                        return
                    }

                    if (!VelocityVanishSpigot.instance.vanishedNames.contains(player.name)) return

                    player.sendActionbar(Message.VANISH_ACTIONBAR, TextReplacement("animation", lastChar))
                }
            }.runTaskTimer(VelocityVanishSpigot.instance, 0, 20)
        }
    }

    fun getSerializedMessage(message: String): String {
        return if (Settings.supportLegacyColorCodes) {
            var legacyMessage = message.replace("&", "§")
            legacyMessage = MiniMessage.miniMessage().serialize(LegacyComponentSerializer.legacySection().deserialize(legacyMessage)).replace("\\<", "<")
            legacyMessage
        } else {
            message
        }
    }

}