package org.sayandev.sayanvanish.velocity

import org.sayandev.sayanvanish.api.Platform
import org.sayandev.sayanvanish.api.PlatformAdapter
import org.sayandev.sayanvanish.api.User
import org.sayandev.sayanvanish.api.VanishUser
import org.sayandev.sayanvanish.velocity.api.VelocityUser
import org.sayandev.sayanvanish.velocity.api.VelocityVanishUser
import org.sayandev.stickynote.velocity.StickyNote
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

object VelocityPlatformAdapter : PlatformAdapter<VelocityUser, VelocityVanishUser> {
    override fun adapt(user: User): VelocityUser {
        val serverId = resolveServerId(user.uniqueId, runCatching { user.serverId }.getOrNull())
        return VelocityUser(user.uniqueId, user.username, user.isOnline, serverId)
    }

    override fun adapt(vanishUser: VanishUser): VelocityVanishUser {
        val serverId = resolveServerId(vanishUser.uniqueId, runCatching { vanishUser.serverId }.getOrNull())
        return VelocityVanishUser(vanishUser.uniqueId, vanishUser.username).also {
            it.serverId = serverId
            it.currentOptions = vanishUser.currentOptions
            it.isVanished = vanishUser.isVanished
            it.vanishLevel = vanishUser.vanishLevel
        }
    }

    fun get(): VelocityPlatformAdapter {
        return this
    }

    private fun resolveServerId(uniqueId: UUID, serverId: String?): String {
        return serverId
            ?: StickyNote.getPlayer(uniqueId)?.currentServer?.getOrNull()?.serverInfo?.name?.takeUnless { it.isEmpty() }
            ?: Platform.get().serverId
    }
}
