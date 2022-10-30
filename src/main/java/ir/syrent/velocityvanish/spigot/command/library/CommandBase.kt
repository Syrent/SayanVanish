package ir.syrent.velocityvanish.spigot.command.library

import org.bukkit.command.CommandSender


/**
 * Interface for all command implementations.
 */
interface CommandBase {
    /**
     * Get command name.
     *
     * @return The name.
     */
    val name: String

    /**
     * Get command permission.
     *
     * @return The permission.
     */
    val permission: String

    /**
     * If only players can execute the command.
     *
     * @return If true.
     */
    val isPlayersOnly: Boolean

    /**
     * Add a subcommand to the command.
     *
     * @param command The subcommand.
     * @return The parent command.
     */
    fun addSubcommand(command: CommandBase): CommandBase

    /**
     * Handle command execution.
     *
     *
     * Marked as default void with no implementation for backwards compatibility.
     *
     * @param sender The sender.
     * @param args   The args.
     */
    fun onExecute(sender: CommandSender, args: List<String>) {
        // Do nothing.
    }

    /**
     * Handle tab completion.
     *
     *
     * Marked as default void with no implementation for backwards compatibility.
     *
     * @param sender The sender.
     * @param args   The args.
     * @return The results.
     */
    fun tabComplete(sender: CommandSender, args: List<String>): List<String> {
        return ArrayList()
    }
}