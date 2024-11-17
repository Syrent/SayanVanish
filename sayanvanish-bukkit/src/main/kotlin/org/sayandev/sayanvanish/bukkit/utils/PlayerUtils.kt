package org.sayandev.sayanvanish.bukkit.utils

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.sayandev.sayanvanish.bukkit.config.language
import org.sayandev.sayanvanish.bukkit.config.settings
import org.sayandev.stickynote.bukkit.utils.AdventureUtils
import org.sayandev.stickynote.bukkit.warn

object PlayerUtils {

    fun CommandSender.sendComponent(content: String, vararg placeholders: TagResolver) {
        val prefix = language.general.prefix
        AdventureUtils.sendComponent(this, if (settings.general.includePrefixInMessages) {
            prefix + content
        } else {
            content
        }, *placeholders)
    }

    fun OfflinePlayer.isPlayingOnThisServer(): Boolean {
        return this.isOnline
    }

}