package ir.syrent.velocityvanish.spigot.command.library

import ir.syrent.velocityvanish.spigot.storage.Message
import ir.syrent.velocityvanish.utils.TextReplacement
import ir.syrent.velocityvanish.spigot.utils.sendMessage
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*
import java.util.stream.Collectors


/**
 * Abstract class for commands that can be handled.
 *
 *
 * Handled commands have a method to pass in raw input from bukkit commands
 * in order to execute the command-specific code. It's essentially an internal
 * layer, hence why it's a package-private class.
 */
abstract class HandledCommand(name: String, permission: String, playersOnly: Boolean) : CommandBase {
    /**
     * Get the command name.
     *
     * @return The name.
     */
    /**
     * The name of the command.
     */
    override val name: String
    /**
     * Get the permission required to execute the command.
     *
     * @return The permission.
     */
    /**
     * The permission required to execute the command.
     *
     *
     * Written out as a string for flexibility with subclasses.
     */
    override val permission: String
    /**
     * Get if the command can only be executed by players.
     *
     * @return If players only.
     */
    /**
     * Should the command only be allowed to be executed by players?
     *
     *
     * In other worlds, only allowed to be executed by console.
     */
    override val isPlayersOnly: Boolean

    /**
     * All subcommands for the command.
     */
    private val subcommands: MutableList<CommandBase>

    /**
     * Create a new command.
     *
     *
     * The name cannot be the same as an existing command as this will conflict.
     *
     * @param name        The name used in execution.
     * @param permission  The permission required to execute the command.
     * @param playersOnly If only players should be able to execute this command.
     */
    init {
        this.name = name
        this.permission = permission
        this.isPlayersOnly = playersOnly
        subcommands = ArrayList<CommandBase>()
    }

    /**
     * Add a subcommand to the command.
     *
     * @param command The subcommand.
     * @return The parent command.
     */
    override fun addSubcommand(command: CommandBase): CommandBase {
        subcommands.add(command)
        return this
    }

    /**
     * Handle the command.
     *
     * @param sender The sender.
     * @param args   The arguments.
     */
    protected fun handle(sender: CommandSender, args: Array<String>) {
        if (!canExecute(sender, this)) {
            return
        }
        if (args.isNotEmpty()) {
            for (subcommand in getSubcommands()) {
                if (subcommand.name.equals(args[0], true)) {
                    if (!canExecute(sender, subcommand)) {
                        return
                    }
                    (subcommand as HandledCommand).handle(sender, args.copyOfRange(1, args.size))
                    return
                }
            }
        }
        if (isPlayersOnly && sender !is Player) {
            sender.sendMessage(Message.ONLY_PLAYERS)
            return
        }
        this.onExecute(sender, listOf(*args))
    }

    /**
     * Handle the tab completion.
     *
     * @param sender The sender.
     * @param args   The arguments.
     * @return The tab completion results.
     */
    protected fun handleTabCompletion(sender: CommandSender, args: Array<String>): List<String>? {
        if (!sender.hasPermission(permission)) {
            return null
        }
        if (args.size == 1) {
            val completions = mutableListOf<String>()
            completions.addAll(
                getSubcommands().stream()
                .filter { subCommand: CommandBase -> sender.hasPermission(subCommand.permission) }
                .map(CommandBase::name)
                .collect(Collectors.toList())
            )
            completions.sorted()
            if (completions.isNotEmpty()) {
                return completions
            }
        }
        if (args.size >= 2) {
            var command: HandledCommand? = null
            for (subcommand in getSubcommands()) {
                if (!sender.hasPermission(subcommand.permission)) {
                    continue
                }
                if (args[0].equals(subcommand.name, ignoreCase = true)) {
                    command = subcommand as HandledCommand
                }
            }
            if (command != null) {
                return command.handleTabCompletion(sender, args.copyOfRange(1, args.size))
            }
        }
        return this.tabComplete(sender, listOf(*args))
    }

    /**
     * Get the subcommands of the command.
     *
     * @return The subcommands.
     */
    fun getSubcommands(): List<CommandBase> {
        return subcommands
    }

    companion object {
        /**
         * If a sender can execute the command.
         *
         * @param sender  The sender.
         * @param command The command.
         * @return If the sender can execute.
         */
        fun canExecute(sender: CommandSender, command: CommandBase): Boolean {
            if (!sender.hasPermission(command.permission) && sender is Player) {
                sender.sendMessage(Message.NO_PERMISSION, TextReplacement("permission", command.permission))
                return false
            }
            return true
        }
    }
}