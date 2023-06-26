package ir.syrent.velocityvanish.command.library.interfaces

import ir.syrent.velocityvanish.spigot.ruom.Ruom

/**
 * Interface for defining commands.
 */
interface ICommand {

    /**
     * Retrieves the permission associated with the specified command literal.
     *
     * @param literal The command literal.
     * @return The permission string.
     */
    fun getPermission(literal: String): String {
        return "${Ruom.getPlugin().name}.commands.$literal"
    }

    /**
     * Sets the prefix for error messages related to this command.
     *
     * @param prefix The error prefix to set.
     */
    fun setErrorPrefix(prefix: String)

}
