package ir.syrent.velocityvanish.spigot.command.vanish

import ir.syrent.velocityvanish.spigot.command.library.SubCommand
import ir.syrent.velocityvanish.spigot.storage.Message
import ir.syrent.velocityvanish.spigot.storage.Settings
import ir.syrent.velocityvanish.spigot.utils.sendMessage
import org.bukkit.command.CommandSender

class ReloadSubcommand : SubCommand("reload", "velocityvanish.command.reload", false) {

    override fun onExecute(sender: CommandSender, args: List<String>) {
        Settings.load()
        sender.sendMessage(Message.RELOAD_USE)
    }
}