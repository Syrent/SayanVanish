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
package org.sayandev.sayanvanish.api.message.types

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import org.sayandev.sayanvanish.api.Platform
import org.sayandev.sayanvanish.api.SayanVanishAPI
import org.sayandev.sayanvanish.api.User
import org.sayandev.sayanvanish.api.VanishAPI
import org.sayandev.sayanvanish.api.VanishUser
import org.sayandev.sayanvanish.api.storage.redis.RedisConfig
import org.sayandev.sayanvanish.api.storage.redis.RedisConnection
import org.sayandev.sayanvanish.api.message.MessagingCategoryTypes
import org.sayandev.sayanvanish.api.message.MessagingService
import org.sayandev.stickynote.core.coroutine.dispatcher.AsyncDispatcher
import org.sayandev.stickynote.core.messaging.MessageMeta
import org.sayandev.stickynote.core.messaging.PayloadBehaviour
import org.sayandev.stickynote.core.messaging.PayloadWrapper
import org.sayandev.stickynote.core.messaging.redis.RedisConnectionMeta
import org.sayandev.stickynote.core.messaging.redis.RedisPublisher
import org.sayandev.stickynote.core.utils.async
import java.util.UUID

// TODO: can't you merge some parts of publisher in another class since both redis and websocket has the same publisher interface?
class RedisMessagingService(
    val config: RedisConfig,
    override val dispatcher: AsyncDispatcher
) : MessagingService {
    val connection = RedisConnection(config, dispatcher)

    val syncUserPublisher = SyncUserPublisher().apply { this.register() }
    val syncVanishUserPublisher = SyncVanishUserPublisher().apply { this.register() }

    override suspend fun syncUser(user: User): Deferred<Boolean> {
        return syncUserPublisher.sync(user)
    }

    override suspend fun syncVanishUser(vanishUser: VanishUser): Deferred<Boolean> {
        return syncVanishUserPublisher.sync(vanishUser)
    }

    override suspend fun shutdown(): Deferred<Boolean> {
        syncUserPublisher.shutdown()
        syncVanishUserPublisher.shutdown()

        if (connection.connected) {
            connection.disconnect().await()
        }

        return CompletableDeferred(true)
    }

    inner class SyncUserPublisher : RedisPublisher<User, Boolean>(
        MessageMeta.create(Platform.get().pluginName.lowercase(), MessagingCategoryTypes.SYNC_USER.id),
        RedisConnectionMeta(connection.redis, dispatcher),
        Platform.get().logger
    ) {
        override fun handle(payload: User): Boolean? {
            async(VanishAPI.get().getDatabase().dispatcher) {
                if (VanishAPI.get().getDatabase().hasUser(payload.uniqueId).await()) {
                    VanishAPI.get().getCacheService().getUsers().put(payload.uniqueId, payload)
                } else {
                    VanishAPI.get().getCacheService().getUsers().remove(payload.uniqueId)
                }
            }
            return true
        }

        suspend fun sync(user: User): CompletableDeferred<Boolean> {
            handle(user)
            return publish(
                PayloadWrapper(
                    uniqueId = UUID.randomUUID(),
                    payload = user,
                    behaviour = PayloadBehaviour.FORWARD,
                    excludeSource = true
                )
            )
        }
    }

    inner class SyncVanishUserPublisher : RedisPublisher<VanishUser, Boolean>(
        MessageMeta.create(Platform.get().pluginName.lowercase(), MessagingCategoryTypes.SYNC_VANISH_USER.id),
        RedisConnectionMeta(connection.redis, dispatcher),
        Platform.get().logger
    ) {
        override fun handle(payload: VanishUser): Boolean? {
            async(VanishAPI.get().getDatabase().dispatcher) {
                if (VanishAPI.get().getDatabase().hasVanishUser(payload.uniqueId).await()) {
                    VanishAPI.get().getCacheService().getVanishUsers().put(payload.uniqueId, payload)
                } else {
                    VanishAPI.get().getCacheService().getVanishUsers().remove(payload.uniqueId)
                }
            }
            return true
        }

        suspend fun sync(vanishUser: VanishUser): CompletableDeferred<Boolean> {
            handle(vanishUser)
            return publish(
                PayloadWrapper(
                    uniqueId = UUID.randomUUID(),
                    payload = vanishUser,
                    behaviour = PayloadBehaviour.FORWARD,
                    excludeSource = true
                )
            )
        }
    }
}
