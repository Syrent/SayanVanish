package org.sayandev.sayanvanish.api

import com.google.gson.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.future.asCompletableFuture
import kotlinx.coroutines.runBlocking
import org.sayandev.sayanvanish.api.database.PlatformTable
import org.sayandev.sayanvanish.api.exception.UnsupportedPlatformException
import org.sayandev.sayanvanish.api.utils.Gson.fromJson
import org.sayandev.sayanvanish.api.utils.Gson.jsonObject
import org.sayandev.stickynote.core.utils.async
import java.lang.reflect.Type
import java.util.*
import java.util.concurrent.CompletableFuture

@Suppress("unused")
interface User {

    val uniqueId: UUID
    var username: String
    var isOnline: Boolean
    var serverId: String

    fun hasPermission(permission: String): Boolean {
        throw UnsupportedPlatformException("hasPermission")
    }

    fun hasPermission(permission: Permission): Boolean {
        return hasPermission(permission.permission())
    }

    @JvmSynthetic
    suspend fun save(): Deferred<Boolean> {
        return SayanVanishAPI.getDatabase().addUser(this)
    }

    fun saveBlocking(): Boolean {
        return runBlocking { save().await() }
    }

    fun saveFuture(): CompletableFuture<Boolean> {
        return async(SayanVanishAPI.get().getDatabase().dispatcher) {
            save().await()
        }.asCompletableFuture()
    }

    @JvmSynthetic
    suspend fun sync(): Deferred<Boolean> {
        return SayanVanishAPI.getMessagingService().syncUser(this)
    }

    fun syncBlocking(): Boolean {
        return runBlocking { sync().await() }
    }

    fun syncFuture(): CompletableFuture<Boolean> {
        return async(SayanVanishAPI.get().getMessagingService().dispatcher) {
            sync().await()
        }.asCompletableFuture()
    }

    @JvmSynthetic
    suspend fun saveAndSync(): List<Deferred<Boolean>> {
        return listOf(
            save(),
            sync()
        )
    }

    fun saveAndSyncBlocking(): List<Boolean> {
        return runBlocking { saveAndSync().map { it.await() } }
    }

    @JvmSynthetic
    suspend fun asVanishUser(): Deferred<VanishUser> {
        return CompletableDeferred<VanishUser>().apply {
            async(SayanVanishAPI.get().getDatabase().dispatcher) {
                SayanVanishAPI.getDatabase().getVanishUser(uniqueId).await() ?: VanishUser.of(uniqueId, username, serverId)
            }.let { complete(it.await()) }
        }
    }

    fun asVanishUserBlocking(): VanishUser {
        return runBlocking { asVanishUser().await() }
    }

    fun asVanishUserFuture(): CompletableFuture<VanishUser> {
        return async(SayanVanishAPI.get().getDatabase().dispatcher) {
            asVanishUser().await()
        }.asCompletableFuture()
    }

    class JsonAdapter : JsonSerializer<User>, JsonDeserializer<User> {
        override fun serialize(src: User, typeOfSrc: Type, context: JsonSerializationContext): JsonObject {
            return JsonParser.parseString(Gson().toJson(src)).asJsonObject
        }

        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): User {
            return Gson().fromJson(json, Generic::class.java)
        }
    }

    object Schema : PlatformTable("users") {
        val uniqueId = uuid("unique_id").uniqueIndex()
        val username = varchar("username", 16)
        val isOnline = bool("is_online").default(false)
        val serverId = varchar("server_id", 36)

        override val primaryKey = PrimaryKey(uniqueId)
    }

    data class Generic(
        override val uniqueId: UUID,
        override var username: String,
        override var isOnline: Boolean,
        override var serverId: String
    ) : User

    fun asGeneric(): Generic {
        return Generic(
            uniqueId,
            username,
            isOnline,
            serverId
        )
    }

    companion object {
        @JvmStatic
        fun of(uniqueId: UUID, username: String, isOnline: Boolean, serverId: String?): User {
            return Generic(
                uniqueId,
                username,
                isOnline,
                serverId ?: Platform.get().serverId
            )
        }

        @JvmStatic
        fun UUID.userFromCache(): User? {
            TODO("Cache is not implemented yet")
        }

        @JvmSynthetic
        suspend fun UUID.user(): Deferred<User?> {
            return SayanVanishAPI.getDatabase().getUser(this)
        }

        @JvmSynthetic
        fun UUID.userBlocking(): User? {
            return runBlocking { user().await() }
        }

        @JvmStatic
        fun getUserBlocking(uniqueId: UUID): User? {
            return uniqueId.userBlocking()
        }

        @JvmSynthetic
        fun UUID.userFuture(): CompletableFuture<User?> {
            return async(SayanVanishAPI.get().getDatabase().dispatcher) {
                user().await()
            }.asCompletableFuture()
        }

        @JvmStatic
        fun getUserFuture(uniqueId: UUID): CompletableFuture<User?> {
            return uniqueId.userFuture()
        }
    }

}