/*
 * This file is part of SayanVanish, licensed under the GNU General Public License v3.0.
 *
 * Copyright (c) 2026 Sayan Development and contributors
 *
 * SayanVanish is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SayanVanish is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
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
