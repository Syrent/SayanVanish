package org.sayandev.sayanvanish.paper.api

import org.sayandev.sayanvanish.api.PlatformAdapter
import org.sayandev.sayanvanish.api.Platform
import org.sayandev.sayanvanish.api.User
import org.sayandev.sayanvanish.api.VanishUser
import org.sayandev.sayanvanish.paper.config.Settings

object PaperPlatformAdapter : PlatformAdapter<PaperUser, PaperVanishUser> {
    override fun adapt(user: User): PaperUser {
        val serverId = if (Settings.get().general.proxyMode) {
            runCatching { user.serverId }.getOrNull() ?: Platform.get().serverId
        } else {
            Platform.get().serverId
        }
        return PaperUser(user.uniqueId, user.username, user.isOnline, serverId)
    }

    override fun adapt(vanishUser: VanishUser): PaperVanishUser {
        val serverId = if (Settings.get().general.proxyMode) {
            runCatching { vanishUser.serverId }.getOrNull() ?: Platform.get().serverId
        } else {
            Platform.get().serverId
        }
        return PaperVanishUser(vanishUser.uniqueId, vanishUser.username).also {
            it.serverId = serverId
            it.currentOptions = vanishUser.currentOptions
            it.isVanished = vanishUser.isVanished
            it.vanishLevel = vanishUser.vanishLevel
        }
    }

    fun get(): PaperPlatformAdapter {
        return this
    }
}
