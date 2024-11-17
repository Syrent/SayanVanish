package org.sayandev.sayanvanish.velocity.utils

import com.velocitypowered.api.command.CommandSource
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.sayandev.sayanvanish.proxy.config.LanguageConfig
import org.sayandev.sayanvanish.proxy.config.language
import org.sayandev.sayanvanish.proxy.config.settings
import org.sayandev.stickynote.velocity.utils.AdventureUtils
import org.sayandev.stickynote.velocity.utils.AdventureUtils.component

object PlayerUtils {
    fun CommandSource.sendComponent(content: String, vararg placeholders: TagResolver) {
        val prefix = language.general.prefix.component()
        val contentComponent = content.component(*placeholders)
        this.sendMessage(if (settings.general.includePrefixInMessages) {
            prefix.append(contentComponent)
        } else {
            contentComponent
        })
    }

    fun CommandSource.sendComponent(content: Component) {
        val prefix = language.general.prefix.component()
        this.sendMessage(if (settings.general.includePrefixInMessages) {
            prefix.append(content)
        } else {
            content
        })
    }
}