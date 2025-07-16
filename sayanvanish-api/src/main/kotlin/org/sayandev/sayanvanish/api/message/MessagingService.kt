package org.sayandev.sayanvanish.api.message

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