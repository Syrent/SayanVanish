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
package org.sayandev.sayanvanish.velocity.utils

import com.velocitypowered.api.command.CommandSource
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.sayandev.sayanvanish.proxy.config.Settings
import org.sayandev.sayanvanish.proxy.config.language
import org.sayandev.stickynote.velocity.utils.AdventureUtils.component

object PlayerUtils {
    fun CommandSource.sendComponent(content: String, vararg placeholders: TagResolver) {
        if (content.isBlank()) return

        val prefix = language.general.prefix.component()
        val contentComponent = content.component(*placeholders)
        this.sendMessage(if (Settings.get().general.includePrefixInMessages) {
            prefix.append(contentComponent)
        } else {
            contentComponent
        })
    }

    fun CommandSource.sendComponent(content: Component) {
        if (content == Component.empty()) return

        val prefix = language.general.prefix.component()
        this.sendMessage(if (Settings.get().general.includePrefixInMessages) {
            prefix.append(content)
        } else {
            content
        })
    }

    fun CommandSource.sendPrefixComponent(content: String, vararg placeholders: TagResolver) {
        if (content.isBlank()) return

        val prefix = language.general.prefix
        val finalContent = if (Settings.get().general.includePrefixInMessages) {
            prefix + content
        } else {
            content
        }

        this.sendComponent(finalContent.component(*placeholders))
    }

    fun CommandSource.sendPrefixComponent(content: Component) {
        val prefix = language.general.prefix
        val finalContent = if (Settings.get().general.includePrefixInMessages) {
            prefix.component().append(content)
        } else {
            content
        }

        this.sendComponent( finalContent)
    }

    fun CommandSource.sendRawComponent(content: String, vararg placeholders: TagResolver) {
        this.sendMessage(content.component(*placeholders))
    }
}