package org.sayandev.sayanvanish.velocity

import org.sayandev.sayanvanish.api.PlatformAdapter
import org.sayandev.sayanvanish.api.VanishUser
import org.sayandev.sayanvanish.velocity.api.VelocityUser
import org.sayandev.sayanvanish.velocity.api.VelocityVanishUser

object VelocityPlatformAdapter : PlatformAdapter<VelocityVanishUser> {
    override fun adapt(user: VanishUser): VelocityVanishUser {
        return VelocityVanishUser(user.uniqueId, user.username)
    }
}