package org.sayandev.sayanvanish.paper.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.sayandev.sayanvanish.paper.config.Settings
import org.sayandev.sayanvanish.paper.config.language
import org.sayandev.stickynote.bukkit.hook.PlaceholderAPIHook
import org.sayandev.stickynote.bukkit.utils.AdventureUtils
import org.sayandev.stickynote.bukkit.utils.AdventureUtils.component

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