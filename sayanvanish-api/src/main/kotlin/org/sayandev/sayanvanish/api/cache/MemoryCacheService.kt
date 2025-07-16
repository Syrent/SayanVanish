package org.sayandev.sayanvanish.api.cache

import org.sayandev.sayanvanish.api.User
import java.util.UUID

class MemoryCacheService : CacheService {
    object Users : ICache<UUID, User> by Cache("users")
    object VanishUsers : ICache<UUID, User> by Cache("vanish_users")
    object UsersCount : ICache<String, Int> by Cache("users_count")

    override fun getUsers() = Users
    override fun getVanishUsers() = VanishUsers
    override fun getUsersCount() = UsersCount
}