package ir.syrent.velocityvanish.spigot.command.library


/**
 * Subcommands can be added to PluginCommands or to other Subcommands.
 */
abstract class SubCommand : HandledCommand {
    /**
     * Create subcommand.
     *
     * @param name        The subcommand name.
     * @param permission  The subcommand permission.
     * @param playersOnly If the subcommand only works on players.
     */
    protected constructor(name: String, permission: String, playersOnly: Boolean) : super(name, permission, playersOnly)

    /**
     * Create subcommand.
     *
     * @param name   The name of the subcommand.
     * @param parent The parent command.
     */
    protected constructor(name: String, parent: CommandBase) : super(name, parent.permission, parent.isPlayersOnly)
}