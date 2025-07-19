package org.sayandev.sayanvanish.bukkit.utils

import org.sayandev.sayanventure.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.sayandev.sayanvanish.bukkit.config.Settings
import org.sayandev.sayanvanish.bukkit.config.language
import org.sayandev.stickynote.bukkit.hook.PlaceholderAPIHook
import org.sayandev.stickynote.bukkit.utils.AdventureUtils
import org.sayandev.stickynote.bukkit.utils.AdventureUtils.component

object PlayerUtils {

    fun CommandSender.sendComponent(content: String, vararg placeholders: TagResolver) {
        if (content.isBlank()) return

        val prefix = language.general.prefix
        val finalContent = if (Settings.get().general.includePrefixInMessages) {
            prefix + content
        } else {
            content
        }

        AdventureUtils.sendComponent(this, PlaceholderAPIHook.injectPlaceholders(this as? Player, finalContent).component(*placeholders))
    }

    fun CommandSender.sendRawComponent(content: String, vararg placeholders: TagResolver) {
        if (content.isBlank()) return
        AdventureUtils.sendComponent(this, PlaceholderAPIHook.injectPlaceholders(this as? Player, content).component(*placeholders))
    }

}