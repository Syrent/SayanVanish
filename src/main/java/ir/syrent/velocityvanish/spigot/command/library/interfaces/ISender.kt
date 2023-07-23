package ir.syrent.velocityvanish.spigot.command.library.interfaces

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

interface ISender {

    /**
     * Retrieves the player associated with the sender.
     *
     * @return The player if the sender is a player, otherwise null.
     */
    fun player(): Player?

    /**
     * Retrieves the audience associated with the sender.
     *
     * @return The audience associated with the sender.
     */
    fun audience(): Audience

    /**
     * Sets the command sender for this sender instance.
     *
     * @param sender The command sender to set.
     */
    fun setSender(sender: CommandSender)

    /**
     * Retrieves the command sender associated with this sender instance.
     *
     * @return The command sender associated with this sender instance.
     */
    fun getSender(): CommandSender

    /**
     * Sends a message to the sender if they are not a player.
     *
     * @param message The message to send.
     */
    fun sentOnlyPlayersMessage(message: Component)
}
