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

    inner class SyncUserPublisher : RedisPublisher<User, Boolean>(
        MessageMeta.create(Platform.get().id.lowercase(), MessagingCategoryTypes.SYNC_USER.id),
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
            return publish(
                PayloadWrapper(
                    user,
                    PayloadBehaviour.FORWARD
                )
            )
        }
    }

    inner class SyncVanishUserPublisher : RedisPublisher<VanishUser, Boolean>(
        MessageMeta.create(Platform.get().id.lowercase(), MessagingCategoryTypes.SYNC_VANISH_USER.id),
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
            return publish(
                PayloadWrapper(
                    vanishUser,
                    PayloadBehaviour.FORWARD
                )
            )
        }
    }
}