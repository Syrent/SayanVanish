package org.sayandev.sayanvanish.api.message.types

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import org.sayandev.sayanvanish.api.Platform
import org.sayandev.sayanvanish.api.User
import org.sayandev.sayanvanish.api.VanishUser
import org.sayandev.sayanvanish.api.database.redis.RedisConfig
import org.sayandev.sayanvanish.api.database.redis.RedisConnection
import org.sayandev.sayanvanish.api.message.MessagingCategoryTypes
import org.sayandev.sayanvanish.api.message.MessagingService
import org.sayandev.stickynote.core.coroutine.dispatcher.AsyncDispatcher
import org.sayandev.stickynote.core.messaging.publisher.PayloadWrapper
import org.sayandev.stickynote.core.messaging.publisher.RedisPublisher

class RedisMessagingService(
    val config: RedisConfig,
    override val dispatcher: AsyncDispatcher
) : MessagingService {
    val connection = RedisConnection(config, dispatcher)

    val syncUserPublisher = SyncUserPublisher()
    val syncVanishUserPublisher = SyncVanishUserPublisher()

    override suspend fun syncUser(user: User): Deferred<Boolean> {
        return syncUserPublisher.sync(user)
    }

    override suspend fun syncVanishUser(vanishUser: VanishUser): Deferred<Boolean> {
        return syncVanishUserPublisher.sync(vanishUser)
    }

    inner class SyncUserPublisher : RedisPublisher<User, Boolean>(
        dispatcher,
        connection.redis,
        Platform.Companion.get().id.lowercase(),
        MessagingCategoryTypes.SYNC_USER.id,
        User::class.java,
        Boolean::class.java,
        Platform.Companion.get().logger
    ) {
        override fun handle(payload: User): Boolean? {
            TODO("Set user in cache")
        }

        suspend fun sync(user: User): CompletableDeferred<Boolean> {
            return publish(
                PayloadWrapper(
                    user,
                    PayloadWrapper.State.FORWARD
                )
            )
        }
    }

    inner class SyncVanishUserPublisher : RedisPublisher<VanishUser, Boolean>(
        dispatcher,
        connection.redis,
        Platform.Companion.get().id.lowercase(),
        MessagingCategoryTypes.SYNC_VANISH_USER.id,
        VanishUser::class.java,
        Boolean::class.java,
        Platform.Companion.get().logger
    ) {
        override fun handle(payload: VanishUser): Boolean? {
            TODO("Set vanish user in cache")
        }

        suspend fun sync(vanishUser: VanishUser): CompletableDeferred<Boolean> {
            return publish(
                PayloadWrapper(
                    vanishUser,
                    PayloadWrapper.State.FORWARD
                )
            )
        }
    }
}