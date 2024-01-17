package ir.syrent.velocityvanish.spigot.utils

import ir.syrent.velocityvanish.spigot.ruom.adventure.AdventureApi
import ir.syrent.velocityvanish.spigot.storage.Message
import ir.syrent.velocityvanish.spigot.storage.Settings
import ir.syrent.velocityvanish.spigot.utils.Utils.getSerializedMessage
import ir.syrent.velocityvanish.utils.TextReplacement
import ir.syrent.velocityvanish.utils.component
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

fun CommandSender.sendMessage(message: Message, vararg replacements: TextReplacement) {
    val formattedMessage = Settings.formatMessage(message, *replacements)
    if (formattedMessage.isBlank()) return

    val serializedMessage = getSerializedMessage(formattedMessage)
    AdventureApi.get().sender(this).sendMessage(serializedMessage.component())
}

fun Player.sendMessage(message: Message, vararg replacements: TextReplacement) {
    this.sendMessage(message, this, *replacements)
}

fun Player.sendMessage(message: Message, placeholderTarget: Player, vararg replacements: TextReplacement) {
    val formattedMessage = Settings.formatMessage(this, message, placeholderTarget, *replacements)
    if (formattedMessage.isBlank()) return

    Settings.commandSound.let {
        if (it != null) {
            this.playSound(this.location, it, 1f, 1f)
        }
    }

    val serializedMessage = getSerializedMessage(formattedMessage)
    AdventureApi.get().sender(this).sendMessage(serializedMessage.component())
}

fun Player.sendMessageOnly(message: Message, vararg replacements: TextReplacement) {
    val serializedMessage = getSerializedMessage(Settings.formatMessage(this, message, this, *replacements))
    AdventureApi.get().sender(this).sendMessage(serializedMessage.component())
}

fun Player.sendActionbar(message: Message, vararg replacements: TextReplacement) {
    val serializedMessage = getSerializedMessage(Settings.formatMessage(this, message, this, *replacements))
    AdventureApi.get().sender(this).sendActionBar(serializedMessage.component())
}