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
package org.sayandev.sayanvanish.paper.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.sayandev.sayanvanish.paper.config.Settings
import org.sayandev.sayanvanish.paper.config.language
import org.sayandev.stickynote.paper.hook.PlaceholderAPIHook
import org.sayandev.stickynote.paper.utils.AdventureUtils
import org.sayandev.stickynote.paper.utils.AdventureUtils.component

object PlayerUtils {

    fun CommandSender.sendPrefixComponent(content: String, vararg placeholders: TagResolver) {
        if (content.isBlank()) return

        val prefix = language.general.prefix
        val finalContent = if (Settings.get().general.includePrefixInMessages) {
            prefix + content
        } else {
            content
        }

        AdventureUtils.sendComponent(this, PlaceholderAPIHook.injectPlaceholders(this as? Player, finalContent).component(*placeholders))
    }

    fun CommandSender.sendPrefixComponent(content: Component) {
        val prefix = language.general.prefix
        val finalContent = if (Settings.get().general.includePrefixInMessages) {
            prefix.component().append(content)
        } else {
            content
        }

        AdventureUtils.sendComponent(this, finalContent)
    }

    fun CommandSender.sendRawComponent(content: String, vararg placeholders: TagResolver) {
        if (content.isBlank()) return
        AdventureUtils.sendComponent(this, PlaceholderAPIHook.injectPlaceholders(this as? Player, content).component(*placeholders))
    }

}