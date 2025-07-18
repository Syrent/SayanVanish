package org.sayandev.sayanvanish.api.message.types

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import org.sayandev.sayanvanish.api.Platform
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
import org.sayandev.stickynote.core.utils.async
import java.net.URI
import java.util.*

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

    inner class SyncVanishUserPublisher : WebSocketPublisher<VanishUser, Boolean>(
        MessageMeta.create(Platform.get().id.lowercase(), MessagingCategoryTypes.SYNC_VANISH_USER.id),
        WebSocketConnectionMeta(URI.create(config.uri), dispatcher),
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