package org.sayandev.sayanvanish.velocity

import org.sayandev.sayanvanish.api.PlatformAdapter
import org.sayandev.sayanvanish.api.User
import org.sayandev.sayanvanish.api.VanishUser
import org.sayandev.sayanvanish.velocity.api.VelocityUser
import org.sayandev.sayanvanish.velocity.api.VelocityVanishUser

object VelocityPlatformAdapter : PlatformAdapter<VelocityUser, VelocityVanishUser> {
    override fun adapt(user: User): VelocityUser {
        return VelocityUser(user.uniqueId, user.username, user.isOnline, user.serverId)
    }

    override fun adapt(vanishUser: VanishUser): VelocityVanishUser {
        return VelocityVanishUser(vanishUser.uniqueId, vanishUser.username).also {
            it.currentOptions = vanishUser.currentOptions
            it.isVanished = vanishUser.isVanished
            it.vanishLevel = vanishUser.vanishLevel
        }
    }

    fun get(): VelocityPlatformAdapter {
        return this
    }
}