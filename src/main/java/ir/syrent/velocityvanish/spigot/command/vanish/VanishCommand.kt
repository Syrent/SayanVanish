package ir.syrent.velocityvanish.spigot.command.vanish

import ir.syrent.velocityvanish.spigot.VelocityVanishSpigot
import ir.syrent.velocityvanish.spigot.command.library.PluginCommand
import ir.syrent.velocityvanish.spigot.ruom.Ruom
import ir.syrent.velocityvanish.spigot.storage.Message
import ir.syrent.velocityvanish.spigot.utils.sendMessage
import ir.syrent.velocityvanish.utils.TextReplacement
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class VanishCommand(
    private val plugin: VelocityVanishSpigot
) : PluginCommand("vanish", "velocityvanish.command.vanish", true) {

    init {
        this.register()
        addSubcommand(ReloadSubcommand())
    }

    override fun onExecute(sender: CommandSender, args: List<String>) {
        sender as Player

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
    }
}