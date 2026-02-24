package org.sayandev.sayanvanish.api

import kotlinx.coroutines.runBlocking
import org.sayandev.sayanvanish.api.cache.CacheService
import org.sayandev.sayanvanish.api.cache.MemoryCacheService
import org.sayandev.sayanvanish.api.message.MessagingService
import org.sayandev.sayanvanish.api.message.TypedMessagingService
import org.sayandev.sayanvanish.api.storage.TransactionDatabase
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

    fun initialize(enableMessaging: Boolean = true) {
        runBlocking {
            database.initialize().await()
            messagingService.initialize(enableMessaging).await()
            cacheService.fetchData()

            for (user in database.getVanishUsers().await().filter { user -> user.serverId == Platform.get().serverId }) {
                user.isOnline = false
                user.saveAndSync()
            }
            database.purgeUsers(Platform.get().serverId)
        }
    }

    fun reloadMessaging(enableMessaging: Boolean) {
        runBlocking {
            messagingService.reload(enableMessaging).await()
        }
    }

    override fun getPlatform(): Platform {
        return Platform.get()
    }

    override fun canSee(user: VanishUser?, target: VanishUser): Boolean {
        if (!target.isVanished) return true
        if (user == null) return false
        if (user.uniqueId == target.uniqueId) return true
        if (!user.hasPermission(Permissions.VANISH)) return false
        return user.vanishLevel >= target.vanishLevel
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
