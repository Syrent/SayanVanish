package org.sayandev.sayanvanish.api.cache

import org.sayandev.sayanvanish.api.User
import org.sayandev.sayanvanish.api.VanishAPI
import org.sayandev.sayanvanish.api.VanishUser
import org.sayandev.sayanvanish.api.cache.caches.IUserCache
import org.sayandev.sayanvanish.api.cache.caches.IVanishUserCache
import org.sayandev.sayanvanish.api.cache.caches.UserCache
import org.sayandev.sayanvanish.api.cache.caches.VanishUserCache

class MemoryCacheService : CacheService {
    suspend fun fetchData() {
        Users.putAll(VanishAPI.get().getDatabase().getUsers().await().filter { it.isOnline }.associateBy(User::uniqueId))
        VanishUsers.putAll(VanishAPI.get().getDatabase().getVanishUsers().await().associateBy(VanishUser::uniqueId))
    }

    object Users : IUserCache by UserCache()
    object VanishUsers : IVanishUserCache by VanishUserCache()

    override fun getUsers() = Users
    override fun getVanishUsers() = VanishUsers
}