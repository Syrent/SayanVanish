package org.sayandev.sayanvanish.api

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.future.asCompletableFuture
import kotlinx.coroutines.runBlocking
import org.sayandev.sayanvanish.api.database.Database
import java.util.*
import java.util.concurrent.CompletableFuture

interface VanishAPI {
    fun getDatabase(): Database

    fun getPlatform(): Platform

    fun isVanished(uniqueId: UUID): Deferred<Boolean>

    fun isVanishedFuture(uniqueId: UUID): CompletableFuture<Boolean> {
        return isVanished(uniqueId).asCompletableFuture()
    }

    fun isVanishedSync(uniqueId: UUID): Boolean {
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
        private val defaultInstance = SayanVanishAPI

        @JvmStatic
        fun get(): SayanVanishAPI {
            return defaultInstance
        }

        @JvmStatic
        suspend fun UUID.vanishUser(): VanishUser? {
            return get().getDatabase().getVanishUser(this).await()
        }
    }

}