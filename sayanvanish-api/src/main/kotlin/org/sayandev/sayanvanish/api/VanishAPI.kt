package org.sayandev.sayanvanish.api

import org.sayandev.sayanvanish.api.cache.CacheService
import org.sayandev.sayanvanish.api.feature.Feature
import org.sayandev.sayanvanish.api.feature.FeatureDiscoveryStrategy
import org.sayandev.sayanvanish.api.feature.FeatureInstantiationStrategy
import org.sayandev.sayanvanish.api.feature.FeatureRegistry
import org.sayandev.sayanvanish.api.message.MessagingService
import org.sayandev.sayanvanish.api.storage.Database
import java.util.*

interface VanishAPI {
    fun getDatabase(): Database

    fun getMessagingService(): MessagingService

    fun getCacheService(): CacheService

    fun getFeatureRegistry(): FeatureRegistry

    fun getPlatform(): Platform

    fun canSee(user: VanishUser?, target: VanishUser): Boolean

    companion object {
        private var defaultInstance: VanishAPI = SayanVanishAPI

        private fun lifecycleApi(): LifecycleVanishAPI {
            return get() as? LifecycleVanishAPI
                ?: throw IllegalStateException("Current VanishAPI instance does not implement LifecycleVanishAPI.")
        }

        @JvmStatic
        fun getDefault(): SayanVanishAPI {
            return SayanVanishAPI
        }

        @JvmStatic
        fun get(): VanishAPI {
            return defaultInstance
        }

        @JvmStatic
        fun database(): Database {
            return get().getDatabase()
        }

        @JvmStatic
        fun messagingService(): MessagingService {
            return get().getMessagingService()
        }

        @JvmStatic
        fun cacheService(): CacheService {
            return get().getCacheService()
        }

        @JvmStatic
        fun featureRegistry(): FeatureRegistry {
            return get().getFeatureRegistry()
        }

        @JvmStatic
        fun platform(): Platform {
            return get().getPlatform()
        }

        @JvmStatic
        @JvmOverloads
        fun initialize(enableMessaging: Boolean = true) {
            lifecycleApi().initialize(enableMessaging)
        }

        @JvmStatic
        fun reloadMessaging(enableMessaging: Boolean) {
            lifecycleApi().reloadMessaging(enableMessaging)
        }

        @JvmStatic
        fun shutdown() {
            lifecycleApi().shutdown()
        }

        @JvmStatic
        @JvmOverloads
        fun reinitialize(enableMessaging: Boolean = true) {
            lifecycleApi().reinitialize(enableMessaging)
        }

        @JvmStatic
        fun useDatabase(database: Database) {
            SayanVanishAPI.useDatabase(database)
        }

        @JvmStatic
        fun useMessagingService(messagingService: MessagingService) {
            SayanVanishAPI.useMessagingService(messagingService)
        }

        @JvmStatic
        fun useCacheService(cacheService: CacheService) {
            SayanVanishAPI.useCacheService(cacheService)
        }

        @JvmStatic
        fun useFeatureRegistry(featureRegistry: FeatureRegistry) {
            SayanVanishAPI.useFeatureRegistry(featureRegistry)
        }

        @JvmStatic
        fun useVisibilityPolicy(visibilityPolicy: VisibilityPolicy) {
            SayanVanishAPI.useVisibilityPolicy(visibilityPolicy)
        }

        @JvmStatic
        fun useFeatureDiscoveryStrategy(strategy: FeatureDiscoveryStrategy) {
            SayanVanishAPI.useFeatureDiscoveryStrategy(strategy)
        }

        @JvmStatic
        fun useFeatureInstantiationStrategy(strategy: FeatureInstantiationStrategy) {
            SayanVanishAPI.useFeatureInstantiationStrategy(strategy)
        }

        @JvmStatic
        fun registerFeatureClass(featureClass: Class<out Feature>) {
            SayanVanishAPI.registerFeatureClass(featureClass)
        }

        @JvmStatic
        fun unregisterFeatureClass(featureClass: Class<out Feature>) {
            SayanVanishAPI.unregisterFeatureClass(featureClass)
        }

        @JvmStatic
        fun clearManualFeatureClasses() {
            SayanVanishAPI.clearManualFeatureClasses()
        }

        @JvmStatic
        fun resetFeatureStrategies() {
            SayanVanishAPI.resetFeatureStrategies()
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
