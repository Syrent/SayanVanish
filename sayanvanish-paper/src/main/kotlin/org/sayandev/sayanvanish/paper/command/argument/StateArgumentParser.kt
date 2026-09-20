/*
 * This file is part of SayanVanish, licensed under the GNU General Public License v3.0.
 *
 * Copyright (c) 2026 Sayan Development and contributors
 *
 * SayanVanish is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SayanVanish is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.sayandev.sayanvanish.paper.command.argument

import dev.jorel.commandapi.arguments.CustomArgument
import dev.jorel.commandapi.arguments.StringArgument
import org.sayandev.stickynote.command.paper.suggest

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
