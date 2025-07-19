package org.sayandev.sayanvanish.bukkit.utils

import org.sayandev.sayanventure.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.command.CommandSender
import org.sayandev.sayanvanish.bukkit.config.Settings
import org.sayandev.sayanvanish.bukkit.config.language
import org.sayandev.stickynote.bukkit.utils.AdventureUtils

object PlayerUtils {

    fun CommandSender.sendComponent(content: String, vararg placeholders: TagResolver) {
        if (content.isBlank()) return

        val prefix = language.general.prefix
        AdventureUtils.sendComponent(this, if (Settings.get().general.includePrefixInMessages) {
            prefix + content
        } else {
            content
        }, *placeholders)
    }

    fun CommandSender.sendRawComponent(content: String, vararg placeholders: TagResolver) {
        if (content.isBlank()) return
        AdventureUtils.sendComponent(this, content, *placeholders)
    }

}