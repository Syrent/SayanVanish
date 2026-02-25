package org.sayandev.sayanvanish.velocity.api

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.sayandev.sayanvanish.api.Permissions
import org.sayandev.sayanvanish.api.User
import org.sayandev.sayanvanish.velocity.VelocityPlatformAdapter
import org.sayandev.sayanvanish.velocity.utils.PlayerUtils.sendPrefixComponent
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

    override fun sendMessageWithPrefix(content: String, vararg placeholders: TagResolver) {
        player()?.sendPrefixComponent(content.component(*placeholders))
    }

    override fun sendActionbar(content: String, vararg placeholders: TagResolver) {
        player()?.sendActionBar(content.component(*placeholders))
    }

    override fun hasPermission(permission: String): Boolean {
        return player()?.hasPermission(permission) ?: false
    }

    override fun hasPermission(permission: Permissions): Boolean {
        return player()?.hasPermission(permission.permission()) ?: false
    }

    companion object {
        @JvmSynthetic
        fun User.velocityAdapt(): VelocityUser {
            return VelocityPlatformAdapter.adapt(this)
        }
    }
}