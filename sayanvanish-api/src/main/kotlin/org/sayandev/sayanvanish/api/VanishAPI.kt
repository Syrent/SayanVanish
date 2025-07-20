package org.sayandev.sayanvanish.api

import org.sayandev.sayanvanish.api.cache.CacheService
import org.sayandev.sayanvanish.api.message.MessagingService
import org.sayandev.sayanvanish.api.storage.Database
import java.util.*

interface VanishAPI {
    fun getDatabase(): Database

    fun getMessagingService(): MessagingService

    fun getCacheService(): CacheService

    fun getPlatform(): Platform

    fun canSee(user: VanishUser?, target: VanishUser): Boolean

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