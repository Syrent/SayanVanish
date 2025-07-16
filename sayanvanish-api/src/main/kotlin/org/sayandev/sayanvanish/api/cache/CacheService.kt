package org.sayandev.sayanvanish.api.cache

import org.sayandev.sayanvanish.api.cache.caches.IUserCache
import org.sayandev.sayanvanish.api.cache.caches.IVanishUserCache

interface CacheService {
    fun getUsers(): IUserCache
    fun getVanishUsers(): IVanishUserCache
}