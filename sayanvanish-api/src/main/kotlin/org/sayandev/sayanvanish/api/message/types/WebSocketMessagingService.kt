package org.sayandev.sayanvanish.api.message.types

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import org.sayandev.sayanvanish.api.Platform
import org.sayandev.sayanvanish.api.SayanVanishAPI
import org.sayandev.sayanvanish.api.User
import org.sayandev.sayanvanish.api.VanishAPI
import org.sayandev.sayanvanish.api.VanishUser
import org.sayandev.sayanvanish.api.message.MessagingCategoryTypes
import org.sayandev.sayanvanish.api.message.MessagingService
import org.sayandev.sayanvanish.api.storage.websocket.WebSocketConfig
import org.sayandev.stickynote.core.coroutine.dispatcher.AsyncDispatcher
import org.sayandev.stickynote.core.messaging.MessageMeta
import org.sayandev.stickynote.core.messaging.PayloadBehaviour
import org.sayandev.stickynote.core.messaging.PayloadWrapper
import org.sayandev.stickynote.core.messaging.websocket.WebSocketConnectionMeta
import org.sayandev.stickynote.core.messaging.websocket.WebSocketPublisher
import java.net.URI

class WebSocketMessagingService(
    val config: WebSocketConfig,
    override val dispatcher: AsyncDispatcher
) : MessagingService {
    val syncUserPublisher = SyncUserPublisher().apply { this.register() }
    val syncVanishUserPublisher = SyncVanishUserPublisher().apply { this.register() }

    override suspend fun syncUser(user: User): Deferred<Boolean> {
        return syncUserPublisher.sync(user)
    }

    override suspend fun syncVanishUser(vanishUser: VanishUser): Deferred<Boolean> {
        return syncVanishUserPublisher.sync(vanishUser)
    }

    inner class SyncUserPublisher : WebSocketPublisher<User, Boolean>(
        MessageMeta.create(Platform.get().id.lowercase(), MessagingCategoryTypes.SYNC_USER.id),
        WebSocketConnectionMeta(URI.create(config.uri), dispatcher),
        Platform.get().logger
    ) {
        override fun handle(payload: User): Boolean? {
            VanishAPI.get().getCacheService().getUsers().put(payload.uniqueId, payload)
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

    inner class SyncVanishUserPublisher : WebSocketPublisher<VanishUser, Boolean>(
        MessageMeta.create(Platform.get().id.lowercase(), MessagingCategoryTypes.SYNC_VANISH_USER.id),
        WebSocketConnectionMeta(URI.create(config.uri), dispatcher),
        Platform.get().logger
    ) {
        override fun handle(payload: VanishUser): Boolean? {
            VanishAPI.get().getCacheService().getVanishUsers().put(payload.uniqueId, payload)
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