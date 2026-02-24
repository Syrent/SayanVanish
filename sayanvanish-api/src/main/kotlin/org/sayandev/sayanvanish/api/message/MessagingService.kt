package org.sayandev.sayanvanish.api.message

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.future.asCompletableFuture
import kotlinx.coroutines.runBlocking
import org.sayandev.sayanvanish.api.User
import org.sayandev.sayanvanish.api.VanishUser
import org.sayandev.stickynote.core.coroutine.dispatcher.AsyncDispatcher
import org.sayandev.stickynote.core.utils.async
import java.util.concurrent.CompletableFuture

interface MessagingService {
    val dispatcher: AsyncDispatcher

    suspend fun initialize(enabled: Boolean = true): Deferred<Boolean> {
        return CompletableDeferred(true)
    }

    fun initializeFuture(enabled: Boolean): CompletableFuture<Boolean> {
        return async(dispatcher) { initialize(enabled).await() }.asCompletableFuture()
    }

    fun initializeFuture(): CompletableFuture<Boolean> {
        return initializeFuture(true)
    }

    fun initializeBlocking(enabled: Boolean): Boolean {
        return runBlocking { initialize(enabled).await() }
    }

    fun initializeBlocking(): Boolean {
        return initializeBlocking(true)
    }

    suspend fun reload(enabled: Boolean): Deferred<Boolean> {
        return initialize(enabled)
    }

    fun reloadFuture(enabled: Boolean): CompletableFuture<Boolean> {
        return async(dispatcher) { reload(enabled).await() }.asCompletableFuture()
    }

    fun reloadBlocking(enabled: Boolean): Boolean {
        return runBlocking { reload(enabled).await() }
    }

    suspend fun shutdown(): Deferred<Boolean> {
        return CompletableDeferred(true)
    }

    fun shutdownFuture(): CompletableFuture<Boolean> {
        return async(dispatcher) { shutdown().await() }.asCompletableFuture()
    }

    fun shutdownBlocking(): Boolean {
        return runBlocking { shutdown().await() }
    }

    suspend fun syncUser(user: User): Deferred<Boolean>

    fun syncUserFuture(user: User): CompletableFuture<Boolean> {
        return async(dispatcher) { syncUser(user).await() }.asCompletableFuture()
    }

    fun syncUserBlocking(user: User): Boolean {
        return runBlocking { syncUser(user).await() }
    }

    suspend fun syncVanishUser(vanishUser: VanishUser): Deferred<Boolean>

    fun syncVanishUserFuture(vanishUser: VanishUser): CompletableFuture<Boolean> {
        return async(dispatcher) { syncVanishUser(vanishUser).await() }.asCompletableFuture()
    }

    fun syncVanishUserBlocking(vanishUser: VanishUser): Boolean {
        return runBlocking { syncVanishUser(vanishUser).await() }
    }
}
