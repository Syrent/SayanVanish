package ir.syrent.velocityvanish.spigot.command.library

import ir.syrent.velocityvanish.spigot.command.library.interfaces.ISender
import ir.syrent.velocityvanish.spigot.ruom.Ruom
import ir.syrent.velocityvanish.spigot.ruom.adventure.AdventureApi
import ir.syrent.velocityvanish.spigot.storage.Message
import ir.syrent.velocityvanish.spigot.storage.Settings
import ir.syrent.velocityvanish.utils.component
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

open class Sender(
    private var commandSender: CommandSender
): ISender {

    var ONLY_PLAYERS_MESSAGE = Settings.formatMessage(Message.ONLY_PLAYERS).component()

    override fun player(): Player? {
        if (commandSender is Player) return (commandSender as Player).player

        AdventureApi.get().sender(commandSender).sendMessage(ONLY_PLAYERS_MESSAGE)
        return null
    }

    override fun audience(): Audience {
        return BukkitAudiences.create(Ruom.getPlugin()).sender(commandSender)
    }

    override fun setSender(sender: CommandSender) {
        commandSender = sender
    }

    override fun getSender(): CommandSender {
        return commandSender
    }

    override fun sentOnlyPlayersMessage(message: Component) {
        ONLY_PLAYERS_MESSAGE = message
    }

}