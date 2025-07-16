package org.sayandev.sayanvanish.api

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.future.asCompletableFuture
import kotlinx.coroutines.runBlocking
import org.sayandev.sayanvanish.api.cache.CacheService
import org.sayandev.sayanvanish.api.storage.Database
import org.sayandev.sayanvanish.api.message.MessagingService
import java.util.*
import java.util.concurrent.CompletableFuture

interface VanishAPI {
    fun getDatabase(): Database

    fun getMessagingService(): MessagingService

    fun getCacheService(): CacheService

    fun getPlatform(): Platform

    fun isVanished(uniqueId: UUID): Deferred<Boolean>

    fun isVanishedFuture(uniqueId: UUID): CompletableFuture<Boolean> {
        return isVanished(uniqueId).asCompletableFuture()
    }

    fun isVanishedBlocking(uniqueId: UUID): Boolean {
        return runBlocking { isVanished(uniqueId).await() }
    }

    fun canSee(user: VanishUser?, target: VanishUser): Boolean

    fun getOnlineVanishUsers(): Deferred<List<VanishUser>>

    fun getOnlineVanishUsersFuture(): CompletableFuture<List<VanishUser>> {
        return getOnlineVanishUsers().asCompletableFuture()
    }

    fun getOnlineVanishedUsers(): Deferred<List<VanishUser>>

    fun getVanishedUsersFuture(): CompletableFuture<List<VanishUser>> {
        return getVanishedUsers().asCompletableFuture()
    }

    fun getVanishedUsers(): Deferred<List<VanishUser>>

    companion object {
        private var defaultInstance: VanishAPI = SayanVanishAPI

        @JvmStatic
        fun getDefault(): SayanVanishAPI {
            return SayanVanishAPI
        }

        @JvmStatic
        fun get(): VanishAPI {
            return defaultInstance
        }

        @JvmStatic
        fun set(instance: VanishAPI, platform: Platform) {
            Platform.get().logger.info("New API instance has been initialized from ${platform.id}")
            Platform.setAndRegister(platform)
            defaultInstance = instance
            Platform.get().logger.info("Platform has been set to ${platform.id} with plugin name ${platform.pluginName}")
        }

        @JvmStatic
        suspend fun UUID.vanishUser(): VanishUser? {
            return get().getDatabase().getVanishUser(this).await()
        }
    }

}