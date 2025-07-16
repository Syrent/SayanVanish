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

    init {
        launch(database.dispatcher) {
            for (user in database.getVanishUsers().await().filter { user -> user.serverId == Platform.get().serverId }) {
                user.isOnline = false
                user.save()
            }
            database.purgeUsers(Platform.get().serverId)
        }
    }

    override fun getPlatform(): Platform {
        return Platform.get()
    }

    override fun isVanished(uniqueId: UUID): Deferred<Boolean> {
        return CompletableDeferred<Boolean>().apply {
            launch(database.dispatcher) {
                complete(database.getVanishUser(uniqueId).await()?.isVanished == true)
            }
        }
    }

    override fun isVanishedBlocking(uniqueId: UUID): Boolean {
        return runBlocking { isVanished(uniqueId).await() }
    }

    override fun canSee(user: VanishUser?, target: VanishUser): Boolean {
        if (!target.isVanished) return true
        val vanishLevel = user?.vanishLevel ?: -1
        return vanishLevel >= target.vanishLevel
    }

    override fun getOnlineVanishUsers(): Deferred<List<VanishUser>> {
        return CompletableDeferred<List<VanishUser>>().apply {
            launch(database.dispatcher) {
                complete(database.getVanishUsers().await().filter { it.isOnline })
            }
        }
    }

    override fun getOnlineVanishedUsers(): Deferred<List<VanishUser>> {
        return CompletableDeferred<List<VanishUser>>().apply {
            launch(database.dispatcher) {
                complete(database.getVanishUsers().await().filter { it.isOnline && it.isVanished })
            }
        }
    }

    override fun getVanishedUsers(): Deferred<List<VanishUser>> {
        return CompletableDeferred<List<VanishUser>>().apply {
            launch(database.dispatcher) {
                complete(database.getVanishUsers().await().filter { it.isVanished })
            }
        }
    }

    suspend fun UUID.user(): VanishUser? {
        return getDatabase().getVanishUser(this).await()
    }

    /**
     * Only for java usage
     */
    @JvmStatic
    fun get(): SayanVanishAPI {
        return this
    }
}