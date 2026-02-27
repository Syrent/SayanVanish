package org.sayandev.sayanvanish.paper.command.argument

import dev.jorel.commandapi.arguments.CustomArgument
import dev.jorel.commandapi.arguments.StringArgument
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.sayandev.stickynote.command.bukkit.suggest
import java.util.UUID

class OfflinePlayerArgumentParser(
    private val notFoundMessage: () -> String,
) {

    fun argument(name: String): CustomArgument<OfflinePlayer, String> {
        return CustomArgument(StringArgument(name)) { info ->
            val player = lookup(info.input())
            if (player.hasPlayedBefore() || player.isOnline) {
                player
            } else {
                throw CustomArgument.CustomArgumentException.fromString(notFoundMessage())
            }
        }.suggest { suggestion ->
            val input = suggestion.currentArg().lowercase()
            Bukkit.getOfflinePlayers()
                .asSequence()
                .mapNotNull { it.name }
                .filter { it.startsWith(input, ignoreCase = true) }
                .take(30)
                .toList()
        }
    }

    private fun lookup(input: String): OfflinePlayer {
        return runCatching {
            Bukkit.getOfflinePlayer(UUID.fromString(input))
        }.getOrElse {
            Bukkit.getOfflinePlayer(input)
        }
    }
}
