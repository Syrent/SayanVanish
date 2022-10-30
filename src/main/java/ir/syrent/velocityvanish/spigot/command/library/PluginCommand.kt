package ir.syrent.velocityvanish.spigot.command.library

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter


/**
 * PluginCommands are the class to be used instead of CommandExecutor,
 * they function as the base command, e.g. `/wanted` would be a base command, with each
 * subsequent argument functioning as subcommands.
 *
 *
 * The command will not be registered until register() is called.
 *
 *
 * The name cannot be the same as an existing command as this will conflict.
 */
abstract class PluginCommand
/**
 * Create a new command.
 *
 * @param name        The name used in execution.
 * @param permission  The permission required to execute the command.
 * @param playersOnly If only players should be able to execute this command.
 */
protected constructor(name: String, permission: String, playersOnly: Boolean) : HandledCommand(name, permission, playersOnly), CommandExecutor, TabCompleter {
    fun register() {
        val command = Bukkit.getPluginCommand(this.name)!!
        command.setExecutor(this)
        command.tabCompleter = this
    }

    /**
     * Internal implementation used to clean up boilerplate.
     * Used for parity with [CommandExecutor.onCommand].
     *
     * @param sender  The executor of the command.
     * @param command The bukkit command.
     * @param label   The name of the executed command.
     * @param args    The arguments of the command (anything after the physical command name)
     * @return If the command was processed by the linked [RUoMPlugin]
     */
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (!command.name.equals(this.name, ignoreCase = true))
            return false
        this.handle(sender, args)
        return true
    }

    /**
     * Internal implementation used to clean up boilerplate.
     * Used for parity with [TabCompleter.onTabComplete].
     *
     * @param sender  The executor of the command.
     * @param command The bukkit command.
     * @param label   The name of the executed command.
     * @param args    The arguments of the command (anything after the physical command name).
     * @return The list of tab-completions.
     */
    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<String>): List<String>? {
        return if (!command.name.equals(this.name, ignoreCase = true)) null
        else this.handleTabCompletion(sender, args)
    }
}