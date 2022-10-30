package ir.syrent.velocityvanish.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

fun String.component(): Component {
    return MiniMessage.miniMessage().deserialize(this)
}

fun String.component(vararg tags: TagResolver): Component {
    return MiniMessage.miniMessage().deserialize(this, *tags)
}