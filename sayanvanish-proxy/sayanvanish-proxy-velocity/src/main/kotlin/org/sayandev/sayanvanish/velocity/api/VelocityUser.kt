package org.sayandev.sayanvanish.velocity.api

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.sayandev.sayanvanish.api.User
import org.sayandev.sayanvanish.velocity.VelocityPlatformAdapter
import org.sayandev.stickynote.velocity.StickyNote
import org.sayandev.stickynote.velocity.utils.AdventureUtils.component
import java.util.*

open class VelocityUser(
    override val uniqueId: UUID,
    override var username: String,
    override var isOnline: Boolean,
    override var serverId: String
) : User {
    fun player() = StickyNote.getPlayer(uniqueId)

    override fun sendMessage(content: String, vararg placeholders: TagResolver) {
        player()?.sendMessage(content.component(*placeholders))
    }

    override fun sendActionbar(content: String, vararg placeholders: TagResolver) {
        player()?.sendActionBar(content.component(*placeholders))
    }

    companion object {
        @JvmSynthetic
        fun User.bukkitAdapt(): VelocityUser {
            return VelocityPlatformAdapter.adapt(this)
        }
    }
}