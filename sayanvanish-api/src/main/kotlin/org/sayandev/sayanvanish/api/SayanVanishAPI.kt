package org.sayandev.sayanvanish.api

import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.future.asCompletableFuture
import org.sayandev.sayanvanish.api.cache.CacheService
import org.sayandev.sayanvanish.api.cache.MemoryCacheService
import org.sayandev.sayanvanish.api.feature.DefaultFeatureRegistry
import org.sayandev.sayanvanish.api.feature.Feature
import org.sayandev.sayanvanish.api.feature.FeatureDiscoveryStrategy
import org.sayandev.sayanvanish.api.feature.FeatureInstantiationStrategy
import org.sayandev.sayanvanish.api.feature.FeatureRegistry
import org.sayandev.sayanvanish.api.feature.RegisteredFeatureHandler
import org.sayandev.sayanvanish.api.message.MessageConfig
import org.sayandev.sayanvanish.api.message.MessagingService
import org.sayandev.sayanvanish.api.message.TypedMessagingService
import org.sayandev.sayanvanish.api.storage.Database
import org.sayandev.sayanvanish.api.storage.StorageConfig
import org.sayandev.sayanvanish.api.storage.TransactionDatabase
import org.sayandev.stickynote.core.utils.async
import java.util.*
import java.util.concurrent.CompletableFuture

object SayanVanishAPI : LifecycleVanishAPI {
    @Volatile
    private var database: Database = TransactionDatabase()

    @Volatile
    private var messagingService: MessagingService = TypedMessagingService()

    @Volatile
    private var cacheService: CacheService = MemoryCacheService()

    @Volatile
    private var featureRegistry: FeatureRegistry = DefaultFeatureRegistry()

    @Volatile
    private var visibilityPolicy: VisibilityPolicy = DefaultVisibilityPolicy

    @Volatile
    private var initialized = false

    @Volatile
    private var messagingEnabled = true

    override fun getDatabase(): Database {
        return database
    }

    override fun getMessagingService(): MessagingService {
        return messagingService
    }

    override fun getCacheService(): CacheService {
        return cacheService
    }

    override fun getFeatureRegistry(): FeatureRegistry {
        return featureRegistry
    }

    @JvmStatic
    fun database(): Database {
        return database
    }

    @JvmStatic
    fun messagingService(): MessagingService {
        return messagingService
    }

    @JvmStatic
    fun cacheService(): CacheService {
        return cacheService
    }

    @JvmStatic
    fun featureRegistry(): FeatureRegistry {
        return featureRegistry
    }

    @JvmStatic
    fun platform(): Platform {
        return Platform.get()
    }

    @JvmStatic
    fun isInitialized(): Boolean {
        return initialized
    }

    @JvmStatic
    fun useDatabase(database: Database) {
        ensureNotInitialized("replace the database")
        this.database = database
    }

    @JvmStatic
    fun useMessagingService(messagingService: MessagingService) {
        ensureNotInitialized("replace the messaging service")
        this.messagingService = messagingService
    }

    @JvmStatic
    fun useCacheService(cacheService: CacheService) {
        ensureNotInitialized("replace the cache service")
        this.cacheService = cacheService
    }

    @JvmStatic
    fun useFeatureRegistry(featureRegistry: FeatureRegistry) {
        ensureNotInitialized("replace the feature registry")
        this.featureRegistry = featureRegistry
    }

    @JvmStatic
    fun useVisibilityPolicy(visibilityPolicy: VisibilityPolicy) {
        ensureNotInitialized("replace the visibility policy")
        this.visibilityPolicy = visibilityPolicy
    }

    @JvmStatic
    fun useFeatureDiscoveryStrategy(strategy: FeatureDiscoveryStrategy) {
        RegisteredFeatureHandler.setDiscoveryStrategy(strategy)
    }

    @JvmStatic
    fun useFeatureInstantiationStrategy(strategy: FeatureInstantiationStrategy) {
        RegisteredFeatureHandler.setInstantiationStrategy(strategy)
    }

    @JvmStatic
    fun registerFeatureClass(featureClass: Class<out Feature>) {
        RegisteredFeatureHandler.registerFeatureClass(featureClass)
    }

    @JvmStatic
    fun unregisterFeatureClass(featureClass: Class<out Feature>) {
        RegisteredFeatureHandler.unregisterFeatureClass(featureClass)
    }

    @JvmStatic
    fun clearManualFeatureClasses() {
        RegisteredFeatureHandler.clearManualFeatureClasses()
    }

    @JvmStatic
    fun resetFeatureStrategies() {
        RegisteredFeatureHandler.resetStrategies()
    }

    @JvmStatic
    fun initialize() {
        initialize(true)
    }

    override fun initialize(enableMessaging: Boolean) {
        if (initialized) {
            shutdown()
        }

        runBlocking {
            if (database is TransactionDatabase) {
                StorageConfig.reload()
            }
            if (messagingService is TypedMessagingService) {
                MessageConfig.reload()
            }

            messagingEnabled = enableMessaging
            database.initialize().await()
            messagingService.initialize(enableMessaging).await()
            cacheService.initialize(database).await()

            for (user in database.getVanishUsers().await().filter { user -> user.serverId == Platform.get().serverId }) {
                user.isOnline = false
                user.saveAndSync().awaitAll()
            }
            database.purgeUsers(Platform.get().serverId).await()
            initialized = true
        }
    }

    override fun reloadMessaging(enableMessaging: Boolean) {
        runBlocking {
            if (messagingService is TypedMessagingService) {
                MessageConfig.reload()
            }
            messagingEnabled = enableMessaging
            messagingService.reload(enableMessaging).await()
        }
    }

    override fun shutdown() {
        runBlocking {
            messagingService.shutdown().await()
            cacheService.clear().await()
            database.disconnect().await()
        }
        initialized = false
    }

    @JvmStatic
    fun reinitialize() {
        reinitialize(messagingEnabled)
    }

    override fun reinitialize(enableMessaging: Boolean) {
        shutdown()
        initialize(enableMessaging)
    }

    override fun getPlatform(): Platform {
        return Platform.get()
    }

    override fun canSee(user: VanishUser?, target: VanishUser): Boolean {
        return visibilityPolicy.canSee(user, target)
    }

    private fun ensureNotInitialized(action: String) {
        check(!initialized) { "Cannot $action after API initialization. Configure components before calling initialize()." }
    }

    suspend fun UUID.user(): VanishUser? {
        return getDatabase().getVanishUser(this).await()
    }

    @JvmStatic
    fun getVanishUserBlocking(uniqueId: UUID): VanishUser? {
        return runBlocking { uniqueId.user() }
    }

    @JvmStatic
    fun getVanishUserFuture(uniqueId: UUID): CompletableFuture<VanishUser?> {
        return async(getDatabase().dispatcher) { uniqueId.user() }.asCompletableFuture()
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
