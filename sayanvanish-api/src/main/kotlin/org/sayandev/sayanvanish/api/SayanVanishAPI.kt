package org.sayandev.sayanvanish.api

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.runBlocking
import org.sayandev.sayanvanish.api.cache.CacheService
import org.sayandev.sayanvanish.api.cache.MemoryCacheService
import org.sayandev.sayanvanish.api.storage.TransactionDatabase
import org.sayandev.sayanvanish.api.message.MessagingService
import org.sayandev.sayanvanish.api.message.TypedMessagingService
import org.sayandev.stickynote.core.utils.launch
import java.util.*

object SayanVanishAPI : VanishAPI {

    private val database = TransactionDatabase()
    private val messagingService = TypedMessagingService()
    private val cacheService = MemoryCacheService()

    override fun getDatabase(): TransactionDatabase {
        return database
    }

    override fun getMessagingService(): MessagingService {
        return messagingService
    }

    override fun getCacheService(): CacheService {
        return cacheService
    }

    fun initialize() {
        runBlocking {
            database.initialize().await()
            messagingService.initialize().await()
            cacheService.fetchData()

            for (user in database.getVanishUsers().await().filter { user -> user.serverId == Platform.get().serverId }) {
                user.isOnline = false
                user.saveAndSync()
            }
            database.purgeUsers(Platform.get().serverId)
        }
    }

    override fun getPlatform(): Platform {
        return Platform.get()
    }

    override fun canSee(user: VanishUser?, target: VanishUser): Boolean {
        if (!target.isVanished) return true
        val vanishLevel = user?.vanishLevel ?: -1
        return vanishLevel >= target.vanishLevel
    }

    suspend fun UUID.user(): VanishUser? {
        return getDatabase().getVanishUser(this).await()
    }

    /**
     * Don't use this method directly, use [VanishAPI.get] instead.
     *
     * This method is only meant to be used by the API itself as a fallback api.
     *
     * @see VanishAPI.get
     */
    @JvmStatic
    fun get(): SayanVanishAPI {
        return this
    }
}