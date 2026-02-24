package org.sayandev.sayanvanish.paper.api

import org.sayandev.sayanvanish.api.PlatformAdapter
import org.sayandev.sayanvanish.api.User
import org.sayandev.sayanvanish.api.VanishUser

object PaperPlatformAdapter : PlatformAdapter<PaperUser, PaperVanishUser> {
    override fun adapt(user: User): PaperUser {
        return PaperUser(user.uniqueId, user.username, user.isOnline, user.serverId)
    }

    override fun adapt(vanishUser: VanishUser): PaperVanishUser {
        return PaperVanishUser(vanishUser.uniqueId, vanishUser.username).also {
            it.currentOptions = vanishUser.currentOptions
            it.isVanished = vanishUser.isVanished
            it.vanishLevel = vanishUser.vanishLevel
        }
    }

    fun get(): PaperPlatformAdapter {
        return this
    }
}