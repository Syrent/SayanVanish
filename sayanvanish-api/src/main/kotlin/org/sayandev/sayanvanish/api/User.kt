package org.sayandev.sayanvanish.api

import com.google.gson.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.future.asCompletableFuture
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.Component
import org.sayandev.sayanvanish.api.storage.PlatformTable
import org.sayandev.sayanvanish.api.exception.UnsupportedPlatformException
import org.sayandev.stickynote.core.utils.async
import java.lang.reflect.Type
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * Represents a basic user with essential data such as unique ID and username.
 * This interface is the building block for more specific user types like [VanishUser].
 *
 * The [User] interface is commonly used for features such as online counter placeholders
 * and provides basic user information and permission checks.
 *
 * @property uniqueId The unique identifier for the user.
 * @property username The username of the user.
 * @property isOnline Whether the user is currently online.
 * @property serverId The ID of the server the user is connected to.
 *
 * @since 1.0.0
 */
@Suppress("unused")
interface User {
    /**
     * The unique identifier for the user.
     */
    val uniqueId: UUID

    /**
     * The username of the user.
     */
    var username: String

    /**
     * Whether the user is currently online.
     */
    var isOnline: Boolean

    /**
     * The ID of the server the user is connected to.
     */
    var serverId: String

    /**
     * Checks if the user has the specified permission string.
     *
     * @param permission The permission string to check.
     * @return True if the user has the permission, false otherwise.
     * @throws UnsupportedPlatformException If the platform does not support permission checks.
     * @since 1.0.0
     */
    fun hasPermission(permission: String): Boolean {
        throw UnsupportedPlatformException("hasPermission")
    }

    /**
     * Checks if the user has the specified [Permission] object.
     *
     * @param permission The [Permission] to check.
     * @return True if the user has the permission, false otherwise.
     * @since 1.0.0
     */
    fun hasPermission(permission: Permission): Boolean {
        return hasPermission(permission.permission())
    }

    fun sendMessage(content: Component) {
        Platform.get().adapter.adapt(this).sendMessage(content)
    }

    fun sendActionbar(content: Component) {
        Platform.get().adapter.adapt(this).sendActionbar(content)
    }

    /**
     * Saves the user asynchronously to the database.
     *
     * @see org.sayandev.sayanvanish.api.storage.Database.saveUser
     * @return A [Deferred] indicating the result of the save operation.
     * @since 2.0.0
     */
    @JvmSynthetic
    suspend fun save(): Deferred<Boolean> {
        return VanishAPI.get().getDatabase().saveUser(this)
    }

    /**
     * Saves the user to the database in a blocking manner.
     *
     * @return True if the save was successful, false otherwise.
     * @since 2.0.0
     */
    fun saveBlocking(): Boolean {
        return runBlocking { save().await() }
    }

    /**
     * Saves the user to the database and returns a [CompletableFuture] for the result.
     *
     * @return A [CompletableFuture] indicating the result of the save operation.
     * @since 2.0.0
     */
    fun saveFuture(): CompletableFuture<Boolean> {
        return async(VanishAPI.get().getDatabase().dispatcher) {
            save().await()
        }.asCompletableFuture()
    }

    suspend fun delete(): Deferred<Boolean> {
        val deferred = CompletableDeferred<Boolean>()
        async(VanishAPI.get().getDatabase().dispatcher) {
            VanishAPI.get().getDatabase().removeUser(uniqueId).await()
            VanishAPI.get().getMessagingService().syncUser(this@User).await()
            VanishAPI.get().getCacheService().getUsers().remove(uniqueId)

            val vanishedUser = VanishAPI.get().getDatabase().getVanishUser(uniqueId).await()
            if (vanishedUser != null) {
                VanishAPI.get().getDatabase().removeVanishUser(uniqueId).await()
                VanishAPI.get().getMessagingService().syncVanishUser(vanishedUser).await()
                VanishAPI.get().getCacheService().getVanishUsers().remove(uniqueId)
            }

            deferred.complete(true)
        }

        return deferred
    }

    /**
     * Synchronizes the user data with the messaging service asynchronously.
     *
     * @return A [Deferred] indicating the result of the sync operation.
     * @since 2.0.0
     */
    @JvmSynthetic
    suspend fun sync(): Deferred<Boolean> {
        return VanishAPI.get().getMessagingService().syncUser(this)
    }

    /**
     * Synchronizes the user data with the messaging service in a blocking manner.
     *
     * @return True if the sync was successful, false otherwise.
     * @since 2.0.0
     */
    fun syncBlocking(): Boolean {
        return runBlocking { sync().await() }
    }

    /**
     * Synchronizes the user data with the messaging service and returns a [CompletableFuture] for the result.
     *
     * @return A [CompletableFuture] indicating the result of the sync operation.
     * @since 2.0.0
     */
    fun syncFuture(): CompletableFuture<Boolean> {
        return async(VanishAPI.get().getMessagingService().dispatcher) {
            sync().await()
        }.asCompletableFuture()
    }

    /**
     * Saves and synchronizes the user data.
     *
     * @return A list of [Deferred] objects indicating the results of the save and sync operations.
     * @since 2.0.0
     */
    @JvmSynthetic
    suspend fun saveAndSync(): List<Deferred<Boolean>> {
        return listOf(
            save(),
            sync()
        )
    }

    /**
     * Saves and synchronizes the user data in a blocking manner.
     *
     * @return A list of Boolean values indicating the results of the save and sync operations.
     * @since 2.0.0
     */
    fun saveAndSyncBlocking(): List<Boolean> {
        return runBlocking { saveAndSync().awaitAll() }
    }

    /**
     * Asynchronously retrieves the [VanishUser] representation of this user.
     *
     * @return A [Deferred] containing the [VanishUser] instance.
     * @since 2.0.0
     */
    @JvmSynthetic
    suspend fun asVanishUser(): Deferred<VanishUser> {
        return CompletableDeferred<VanishUser>().apply {
            async(VanishAPI.get().getDatabase().dispatcher) {
                VanishAPI.get().getDatabase().getVanishUser(uniqueId).await() ?: VanishUser.Generic(uniqueId, username, serverId)
            }.let { complete(it.await()) }
        }
    }

    /**
     * Retrieves the [VanishUser] representation of this user in a blocking manner.
     *
     * @return The [VanishUser] instance.
     * @since 2.0.0
     */
    fun asVanishUserBlocking(): VanishUser {
        return runBlocking { asVanishUser().await() }
    }

    /**
     * Asynchronously retrieves the [VanishUser] representation of this user and returns a [CompletableFuture].
     *
     * @return A [CompletableFuture] containing the [VanishUser] instance.
     * @since 2.0.0
     */
    fun asVanishUserFuture(): CompletableFuture<VanishUser> {
        return async(VanishAPI.get().getDatabase().dispatcher) {
            asVanishUser().await()
        }.asCompletableFuture()
    }

    fun generatedVanishUser(): VanishUser {
        return VanishUser.Generic(uniqueId, username, serverId)
    }

    /**
     * @since 2.0.0
     */
    // TODO: use kotlinx-serialization-gson?
    class JsonAdapter : JsonSerializer<User>, JsonDeserializer<User> {
        override fun serialize(src: User, typeOfSrc: Type, context: JsonSerializationContext): JsonObject {
            return JsonParser.parseString(Gson().toJson(src)).asJsonObject
        }

        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): User {
            return Gson().fromJson(json, Generic::class.java)
        }
    }

    /**
     * Represents the database schema for the [User] entity.
     *
     * Defines the columns and primary key for the users table.
     *
     * @since 2.0.0
     */
    object Schema : PlatformTable("users") {
        val uniqueId = uuid("unique_id").uniqueIndex()
        val username = varchar("username", 16)
        val isOnline = bool("is_online").default(false)
        val serverId = varchar("server_id", 36)

        override val primaryKey = PrimaryKey(uniqueId)
    }

    /**
     * Generic implementation of the [User] interface.
     *
     * @property uniqueId The unique identifier for the user.
     * @property username The username of the user.
     * @property isOnline Whether the user is currently online.
     * @property serverId The ID of the server the user is connected to.
     * @since 2.0.0
     */
    data class Generic(
        override val uniqueId: UUID,
        override var username: String,
        override var isOnline: Boolean,
        override var serverId: String
    ) : User

    companion object {
        /**
         * Retrieves a [User] from the cache by [UUID].
         *
         * @receiver The [UUID] of the user to retrieve.
         * @return The [User] if found in cache, or null if not found.
         * @since 2.0.0
         */
        @JvmSynthetic
        fun UUID.userFromCache(): User? {
            return VanishAPI.get().getCacheService().getUsers()[this]
        }

        /**
         * Asynchronously retrieves a [User] by [UUID] from the database.
         *
         * @receiver The [UUID] of the user to retrieve.
         * @return A [Deferred] containing the [User] if found, or null if not found.
         * @since 2.0.0
         */
        @JvmSynthetic
        suspend fun UUID.user(): Deferred<User?> {
            return VanishAPI.get().getDatabase().getUser(this)
        }

        /**
         * Retrieves a [User] by [UUID] from the database in a blocking manner.
         *
         * @receiver The [UUID] of the user to retrieve.
         * @return The [User] if found, or null if not found.
         * @since 2.0.0
         */
        @JvmSynthetic
        fun UUID.userBlocking(): User? {
            return runBlocking { user().await() }
        }

        /**
         * Retrieves a [User] by [UUID] from the database in a blocking manner.
         *
         * @param uniqueId The [UUID] of the user to retrieve.
         * @return The [User] if found, or null if not found.
         * @since 2.0.0
         */
        @JvmStatic
        fun getUserBlocking(uniqueId: UUID): User? {
            return uniqueId.userBlocking()
        }

        /**
         * Asynchronously retrieves a [User] by [UUID] from the database and returns a [CompletableFuture].
         *
         * @receiver The [UUID] of the user to retrieve.
         * @return A [CompletableFuture] containing the [User] if found, or null if not found.
         * @since 2.0.0
         */
        @JvmSynthetic
        fun UUID.userFuture(): CompletableFuture<User?> {
            return async(VanishAPI.get().getDatabase().dispatcher) {
                user().await()
            }.asCompletableFuture()
        }

        /**
         * Retrieves a [User] by [UUID] from the database and returns a [CompletableFuture].
         *
         * @param uniqueId The [UUID] of the user to retrieve.
         * @return A [CompletableFuture] containing the [User] if found, or null if not found.
         * @since 2.0.0
         */
        @JvmStatic
        fun getUserFuture(uniqueId: UUID): CompletableFuture<User?> {
            return uniqueId.userFuture()
        }
    }

}