package org.sayandev.sayanvanish.bukkit.api

import org.sayandev.sayanvanish.api.PlatformAdapter
import org.sayandev.sayanvanish.api.User
import org.sayandev.sayanvanish.api.VanishUser

object BukkitPlatformAdapter : PlatformAdapter<BukkitUser, BukkitVanishUser> {
    override fun adapt(user: User): BukkitUser {
        return BukkitUser(user.uniqueId, user.username, user.isOnline, user.serverId)
    }

    override fun adapt(vanishUser: VanishUser): BukkitVanishUser {
        return BukkitVanishUser(vanishUser.uniqueId, vanishUser.username).also {
            it.currentOptions = vanishUser.currentOptions
            it.isVanished = vanishUser.isVanished
            it.vanishLevel = vanishUser.vanishLevel
        }
    }

    fun get(): BukkitPlatformAdapter {
        return this
    }
}