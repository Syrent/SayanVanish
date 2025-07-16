package org.sayandev.sayanvanish.api.cache

import org.sayandev.sayanvanish.api.cache.caches.IUserCache
import org.sayandev.sayanvanish.api.cache.caches.IVanishUserCache
import org.sayandev.sayanvanish.api.cache.caches.UserCache
import org.sayandev.sayanvanish.api.cache.caches.VanishUserCache

class MemoryCacheService : CacheService {
    object Users : IUserCache by UserCache()
    object VanishUsers : IVanishUserCache by VanishUserCache()

    override fun getUsers() = Users
    override fun getVanishUsers() = VanishUsers
}