package org.sayandev.sayanvanish.bungeecord.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.md_5.bungee.api.CommandSender
import org.sayandev.sayanvanish.proxy.config.language
import org.sayandev.sayanvanish.proxy.config.settings
import org.sayandev.stickynote.bungeecord.utils.AdventureUtils.component
import org.sayandev.stickynote.bungeecord.utils.AdventureUtils.sendMessage

object PlayerUtils {
    fun CommandSender.sendComponent(content: String, vararg placeholders: TagResolver) {
        if (content.isBlank()) return

        val prefix = language.general.prefix.component()
        val contentComponent = content.component(*placeholders)
        this.sendMessage(if (settings.general.includePrefixInMessages) {
            prefix.append(contentComponent)
        } else {
            contentComponent
        })
    }

    fun CommandSender.sendComponent(content: Component) {
        if (content == Component.empty()) return

        val prefix = language.general.prefix.component()
        this.sendMessage(if (settings.general.includePrefixInMessages) {
            prefix.append(content)
        } else {
            content
        })
    }
}