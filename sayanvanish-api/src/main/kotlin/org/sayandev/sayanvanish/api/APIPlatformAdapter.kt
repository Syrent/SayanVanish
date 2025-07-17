package org.sayandev.sayanvanish.api

class APIPlatformAdapter : PlatformAdapter<User, VanishUser> {
    override fun adapt(user: User): User {
        return user
    }

    override fun adapt(vanishUser: VanishUser): VanishUser {
        return vanishUser
    }
}