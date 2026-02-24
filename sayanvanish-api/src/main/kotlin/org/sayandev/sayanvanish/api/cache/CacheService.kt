package org.sayandev.sayanvanish.api.cache

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.runBlocking
import org.sayandev.sayanvanish.api.storage.Database
import org.sayandev.sayanvanish.api.cache.caches.IUserCache
import org.sayandev.sayanvanish.api.cache.caches.IVanishUserCache

interface CacheService {
    fun getUsers(): IUserCache
    fun getVanishUsers(): IVanishUserCache

    suspend fun initialize(database: Database): Deferred<Boolean> {
        return CompletableDeferred(true)
    }

    fun initializeBlocking(database: Database): Boolean {
        return runBlocking { initialize(database).await() }
    }

    suspend fun clear(): Deferred<Boolean> {
        return CompletableDeferred(true)
    }

    fun clearBlocking(): Boolean {
        return runBlocking { clear().await() }
    }
}
