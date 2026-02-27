package org.sayandev.sayanvanish.paper.command.argument

import dev.jorel.commandapi.arguments.CustomArgument
import dev.jorel.commandapi.arguments.StringArgument
import org.sayandev.stickynote.command.bukkit.suggest

class StateArgumentParser(
    private val invalidStateMessage: () -> String,
    private val allowedStates: Set<String> = setOf("on", "off"),
) {

    fun argument(name: String): CustomArgument<String, String> {
        return CustomArgument(StringArgument(name)) { info ->
            val state = info.input().lowercase()
            if (state in allowedStates) {
                state
            } else {
                throw CustomArgument.CustomArgumentException.fromString(invalidStateMessage())
            }
        }.suggest(allowedStates.toList())
    }
}
