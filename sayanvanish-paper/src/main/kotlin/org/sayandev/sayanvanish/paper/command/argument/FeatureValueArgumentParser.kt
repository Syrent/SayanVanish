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
import dev.jorel.commandapi.arguments.GreedyStringArgument
import org.sayandev.stickynote.command.paper.suggest

class FeatureValueArgumentParser(
    private val invalidOptionMessage: () -> String,
    private val invalidValueMessage: (String) -> String,
) {

    fun argument(name: String): CustomArgument<Any, String> {
        return CustomArgument(GreedyStringArgument(name)) { info ->
            val option = info.previousArgs().getByClass("option", FeatureOption::class.java)
                ?: throw CustomArgument.CustomArgumentException.fromString(invalidOptionMessage())
            parse(option.field.type, info.input())
        }.suggest { suggestion ->
            val option = suggestion.previousArgs().getByClass("option", FeatureOption::class.java) ?: return@suggest emptyList()
            suggestions(option.field.type)
        }
    }

    private fun parse(type: Class<*>, raw: String): Any {
        return when {
            type == String::class.java -> raw
            type == Int::class.java || type == Integer::class.java ->
                raw.toIntOrNull() ?: invalid("integer")
            type == Long::class.java || type == java.lang.Long::class.java ->
                raw.toLongOrNull() ?: invalid("long")
            type == Float::class.java || type == java.lang.Float::class.java ->
                raw.toFloatOrNull() ?: invalid("float")
            type == Double::class.java || type == java.lang.Double::class.java ->
                raw.toDoubleOrNull() ?: invalid("double")
            type == Boolean::class.java || type == java.lang.Boolean::class.java ->
                raw.toBooleanStrictOrNull() ?: invalid("true, false")
            type.isEnum -> {
                val constants = type.enumConstants.map { (it as Enum<*>).name }
                val matched = constants.firstOrNull { it.equals(raw, ignoreCase = true) }
                    ?: invalid(constants.joinToString(", "))
                type.enumConstants.first { (it as Enum<*>).name == matched }
            }
            else -> invalid(type.simpleName ?: "N/A")
        }
    }

    private fun suggestions(type: Class<*>): Collection<String> {
        return when {
            type == Boolean::class.java || type == java.lang.Boolean::class.java -> listOf("true", "false")
            type == Int::class.java || type == Integer::class.java -> listOf("0", "1", "5", "10", "50", "100")
            type == Long::class.java || type == java.lang.Long::class.java -> listOf("0", "1", "5", "10", "50", "100")
            type == Float::class.java || type == java.lang.Float::class.java -> listOf("0.0", "0.5", "1.0", "5.0", "10.0")
            type == Double::class.java || type == java.lang.Double::class.java -> listOf("0.0", "0.5", "1.0", "5.0", "10.0")
            type.isEnum -> type.enumConstants.map { (it as Enum<*>).name.lowercase() }
            type == String::class.java -> listOf("\"value\"")
            else -> listOf("value")
        }
    }

    private fun invalid(expected: String): Nothing {
        throw CustomArgument.CustomArgumentException.fromString(invalidValueMessage(expected))
    }
}
