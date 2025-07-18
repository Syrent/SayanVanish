package org.sayandev.sayanvanish.api

import com.google.gson.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.future.asCompletableFuture
import kotlinx.coroutines.runBlocking
import org.sayandev.sayanvanish.api.User.Companion.user
import org.sayandev.sayanvanish.api.storage.PlatformTable
import org.sayandev.stickynote.core.utils.async
import org.sayandev.stickynote.core.utils.launch
import java.lang.reflect.Type
import java.util.*
import java.util.concurrent.CompletableFuture

interface VanishUser : User {

    var currentOptions: VanishOptions

    var isVanished: Boolean

    var vanishLevel: Int

    // TODO: better implementation for this
    fun stateText(isVanished: Boolean = this.isVanished) = if (isVanished) "<green>ON</green>" else "<red>OFF</red>"

    fun disappear(options: VanishOptions) {
        isVanished = true
        launch(VanishAPI.get().getDatabase().dispatcher) {
            saveAndSync()
        }
    }

    fun disappear() {
        disappear(VanishOptions.defaultOptions())
    }

    fun appear(options: VanishOptions) {
        isVanished = false
        launch(VanishAPI.get().getDatabase().dispatcher) {
            saveAndSync()
        }
    }

    fun appear() {
        appear(VanishOptions.defaultOptions())
    }

    suspend fun toggleVanish(options: VanishOptions) {
        if (isVanished) appear(options) else disappear(options)
    }

    suspend fun toggleVanish() {
        toggleVanish(VanishOptions.defaultOptions())
    }

    /**
    * @param otherVanishUser The user to check if this user can see
    * */
    fun canSee(otherVanishUser: VanishUser): Boolean {
        if (!otherVanishUser.isVanished) return true
        if (this.uniqueId == otherVanishUser.uniqueId) return true
        val canSee = vanishLevel >= otherVanishUser.vanishLevel
        return canSee
    }

    @JvmSynthetic
    override suspend fun save(): Deferred<Boolean> {
        val deferred = CompletableDeferred<Boolean>()
        async(VanishAPI.get().getDatabase().dispatcher) {
            listOf(
                super.save(),
                VanishAPI.get().getDatabase().saveVanishUser(this@VanishUser)
            ).awaitAll()
            deferred.complete(true)
        }
        return deferred
    }

    override fun saveBlocking(): Boolean {
        return runBlocking { save().await() }
    }

    override fun saveFuture(): CompletableFuture<Boolean> {
        return async(VanishAPI.get().getDatabase().dispatcher) {
            save().await()
        }.asCompletableFuture()
    }

    override suspend fun delete(): Deferred<Boolean> {
        val deferred = CompletableDeferred<Boolean>()
        async(VanishAPI.get().getDatabase().dispatcher) {
            VanishAPI.get().getDatabase().removeVanishUser(uniqueId).await()
            VanishAPI.get().getMessagingService().syncVanishUser(this@VanishUser).await()
            VanishAPI.get().getCacheService().getVanishUsers().remove(uniqueId)

            deferred.complete(true)
        }

        return deferred
    }

    @JvmSynthetic
    override suspend fun sync(): Deferred<Boolean> {
        val deferred = CompletableDeferred<Boolean>()
        async(VanishAPI.get().getDatabase().dispatcher) {
            listOf(
                super.sync(),
                VanishAPI.get().getMessagingService().syncVanishUser(this@VanishUser)
            ).awaitAll()
            deferred.complete(true)
        }
        return deferred
    }

    override fun syncBlocking(): Boolean {
        return runBlocking { sync().await() }
    }

    override fun syncFuture(): CompletableFuture<Boolean> {
        return async(VanishAPI.get().getDatabase().dispatcher) {
            sync().await()
        }.asCompletableFuture()
    }

    @JvmSynthetic
    override suspend fun saveAndSync(): List<Deferred<Boolean>> {
        return listOf(
            save(),
            sync()
        )
    }

    override fun saveAndSyncBlocking(): List<Boolean> {
        return runBlocking { saveAndSync().awaitAll() }
    }

    override fun adapt(): VanishUser {
        return Platform.get().adapter.adapt(this)
    }

    class JsonAdapter : JsonSerializer<VanishUser>, JsonDeserializer<VanishUser> {
        override fun serialize(src: VanishUser, typeOfSrc: Type, context: JsonSerializationContext): JsonObject {
            return JsonParser.parseString(Gson().toJson(src)).asJsonObject
        }

        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): VanishUser {
            return Gson().fromJson(json, Generic::class.java)
        }
    }

    object Schema : PlatformTable("vanish_users") {
        val uniqueId = reference("unique_id", User.Schema.uniqueId).uniqueIndex()
        val isVanished = bool("is_vanished")
        val vanishLevel = integer("vanish_level")
        val currentOptions = varchar("current_options", 255)

        override val primaryKey = PrimaryKey(uniqueId)
    }

    data class Generic(
        override val uniqueId: UUID,
        override var username: String,
        override var serverId: String = Platform.get().serverId,
        override var isVanished: Boolean = false,
        override var isOnline: Boolean = false,
        override var vanishLevel: Int = 1,
        override var currentOptions: VanishOptions = VanishOptions.defaultOptions()
    ): VanishUser

    companion object {
        @JvmSynthetic
        fun UUID.vanishUserFromCache(): VanishUser? {
            return VanishAPI.get().getCacheService().getVanishUsers().getVanishUser(this)
        }

        @JvmSynthetic
        suspend fun UUID.vanishUser(): Deferred<VanishUser?> {
            return VanishAPI.get().getDatabase().getVanishUser(this)
        }

        @JvmSynthetic
        fun UUID.vanishUserBlocking(): VanishUser? {
            return runBlocking { vanishUser().await() }
        }

        @JvmStatic
        fun getUserBlocking(uniqueId: UUID): VanishUser? {
            return uniqueId.vanishUserBlocking()
        }

        @JvmSynthetic
        fun UUID.vanishUserFuture(): CompletableFuture<VanishUser?> {
            return async(VanishAPI.get().getDatabase().dispatcher) {
                vanishUser().await()
            }.asCompletableFuture()
        }

        @JvmStatic
        fun getVanishUserFuture(uniqueId: UUID): CompletableFuture<VanishUser?> {
            return uniqueId.vanishUserFuture()
        }
    }

}