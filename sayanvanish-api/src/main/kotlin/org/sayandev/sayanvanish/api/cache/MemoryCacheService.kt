package org.sayandev.sayanvanish.api.cache

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import org.sayandev.sayanvanish.api.User
import org.sayandev.sayanvanish.api.VanishUser
import org.sayandev.sayanvanish.api.storage.Database
import org.sayandev.sayanvanish.api.cache.caches.IUserCache
import org.sayandev.sayanvanish.api.cache.caches.IVanishUserCache
import org.sayandev.sayanvanish.api.cache.caches.UserCache
import org.sayandev.sayanvanish.api.cache.caches.VanishUserCache

class MemoryCacheService : CacheService {
    override suspend fun initialize(database: Database): Deferred<Boolean> {
        Users.clear()
        VanishUsers.clear()

        Users.putAll(database.getUsers().await().filter { it.isOnline }.associateBy(User::uniqueId))
        VanishUsers.putAll(database.getVanishUsers().await().associateBy(VanishUser::uniqueId))
        return CompletableDeferred(true)
    }

    override suspend fun clear(): Deferred<Boolean> {
        Users.clear()
        VanishUsers.clear()
        return CompletableDeferred(true)
    }

    object Users : IUserCache by UserCache()
    object VanishUsers : IVanishUserCache by VanishUserCache()

    override fun getUsers() = Users
    override fun getVanishUsers() = VanishUsers
}
