package ir.syrent.velocityvanish.spigot.utils

import ir.syrent.velocityvanish.spigot.ruom.adventure.AdventureApi
import ir.syrent.velocityvanish.spigot.storage.Message
import ir.syrent.velocityvanish.spigot.storage.Settings
import ir.syrent.velocityvanish.utils.TextReplacement
import ir.syrent.velocityvanish.utils.component
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

fun CommandSender.sendMessage(message: Message, vararg replacements: TextReplacement) {
    AdventureApi.get().sender(this).sendMessage(Settings.formatMessage(message, *replacements).component())
}

fun Player.sendMessage(message: Message, vararg replacements: TextReplacement) {
    val formattedMessage = Settings.formatMessage(this, message, *replacements)
    if (formattedMessage.isBlank()) return

    Settings.commandSound.let {
        if (it != null) {
            this.playSound(this.location, it, 1f, 1f)
        }
    }
    AdventureApi.get().sender(this).sendMessage(formattedMessage.component())
}

fun Player.sendMessageOnly(message: Message, vararg replacements: TextReplacement) {
    AdventureApi.get().sender(this).sendMessage(Settings.formatMessage(this, message, *replacements).component())
}

fun Player.sendActionbar(message: Message, vararg replacements: TextReplacement) {
    AdventureApi.get().sender(this).sendActionBar(Settings.formatMessage(this, message, *replacements).component())
}