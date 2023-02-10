package ir.syrent.velocityvanish.spigot.command.vanish

import ir.syrent.velocityvanish.spigot.VelocityVanishSpigot
import ir.syrent.velocityvanish.spigot.command.library.PluginCommand
import ir.syrent.velocityvanish.spigot.ruom.Ruom
import ir.syrent.velocityvanish.spigot.storage.Message
import ir.syrent.velocityvanish.spigot.utils.sendMessage
import ir.syrent.velocityvanish.utils.TextReplacement
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class VanishCommand(
    private val plugin: VelocityVanishSpigot
) : PluginCommand("vanish", "velocityvanish.command.vanish", true) {

    init {
        this.register()
        addSubcommand(ReloadSubcommand())
        addSubcommand(OnSubcommand(plugin))
        addSubcommand(OffSubcommand(plugin))
    }

    override fun onExecute(sender: CommandSender, args: List<String>) {
        sender as Player

        if (args.isEmpty()) {
            if (plugin.vanishedNames.contains(sender.name)) {
                sender.sendMessage(Message.VANISH_USE_UNVANISH)
                plugin.vanishManager.unVanish(sender)
            } else {
                sender.sendMessage(Message.VANISH_USE_VANISH)
                plugin.vanishManager.vanish(sender)
            }

            for (staff in Ruom.getOnlinePlayers().filter { it.hasPermission("velocityvanish.admin.notify") && it != sender }) {
                if (!plugin.vanishedNames.contains(sender.name)) {
                    staff.sendMessage(Message.UNVANISH_NOTIFY, TextReplacement("player", sender.name))
                } else {
                    staff.sendMessage(Message.VANISH_NOTIFY, TextReplacement("player", sender.name))
                }
            }
        } else {
            val target = Bukkit.getPlayerExact(args[0])

            if (target == null) {
                sender.sendMessage(Message.PLAYER_NOT_FOUND)
                return
            }

            if (plugin.vanishedNames.contains(target.name)) {
                target.sendMessage(Message.VANISH_USE_UNVANISH)
                plugin.vanishManager.unVanish(target)
            } else {
                target.sendMessage(Message.VANISH_USE_VANISH)
                plugin.vanishManager.vanish(target)
            }

            for (staff in Ruom.getOnlinePlayers().filter { it.hasPermission("velocityvanish.admin.notify") && it != target }) {
                if (!plugin.vanishedNames.contains(target.name)) {
                    staff.sendMessage(Message.UNVANISH_NOTIFY, TextReplacement("player", target.name))
                } else {
                    staff.sendMessage(Message.VANISH_NOTIFY, TextReplacement("player", target.name))
                }
            }
        }
    }

}