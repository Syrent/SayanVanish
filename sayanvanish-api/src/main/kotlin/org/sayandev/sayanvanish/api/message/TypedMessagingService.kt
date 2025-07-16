package org.sayandev.sayanvanish.api.message

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import org.sayandev.sayanvanish.api.Platform
import org.sayandev.sayanvanish.api.User
import org.sayandev.sayanvanish.api.VanishUser
import org.sayandev.sayanvanish.api.message.types.RedisMessagingService
import org.sayandev.sayanvanish.api.message.types.WebSocketMessagingService
import org.sayandev.stickynote.core.coroutine.dispatcher.AsyncDispatcher

class TypedMessagingService: MessagingService {
    override val dispatcher =
        AsyncDispatcher(
            "${Platform.get().pluginName.lowercase()}-messaging-thread",
            messageConfig.threadCount
        )

    val messageTypes = mutableMapOf<MessagingTypes, MessagingService>()
    var messagingConnected: Boolean = true

    suspend fun initialize(): Deferred<Boolean> {
        val messagingTypes = messageConfig.categoryTypes.map { it.type }.distinct()
        for (method in messagingTypes) {
            when {
                method == MessagingTypes.REDIS -> {
                    messageTypes[MessagingTypes.REDIS] = try {
                        RedisMessagingService(messageConfig.redis, dispatcher).also { redisMessaging ->
                            redisMessaging.connection.connect()
                            redisMessaging.connection.initialize()
                        }
                    } catch (e: Exception) {
                        messagingConnected = false
                        logMessagingConnectionError()
                        throw e
                    }
                }
                method == MessagingTypes.WEBSOCKET -> {
                    messageTypes[MessagingTypes.WEBSOCKET] = try {
                        WebSocketMessagingService(messageConfig.webSocketConfig, dispatcher)
                    } catch (e: Exception) {
                        messagingConnected = false
                        logMessagingConnectionError()
                        throw e
                    }
                }
                // MessagingTypes.PLUGIN_MESSAGE Will be added on bukkit/proxy side in minecraft platform
            }
        }
        return CompletableDeferred(true)
    }

    inline fun <reified M: MessagingService> getByType(): M {
        return messageTypes.values.filterIsInstance<M>().firstOrNull()
            ?: throw IllegalArgumentException("Received database with type `${M::class.simpleName}` but it isn't registered in the TransactionDatabase database types.")
    }

    override suspend fun syncUser(user: User): Deferred<Boolean> {
        return service(MessagingCategoryTypes.SYNC_USER).syncUser(user)
    }

    override suspend fun syncVanishUser(vanishUser: VanishUser): Deferred<Boolean> {
        return service(MessagingCategoryTypes.SYNC_USER).syncUser(vanishUser)
    }

    fun service(type: MessagingType): MessagingService {
        return messageTypes[type] ?: let {
            val (fallbackMethod, fallbackService) = messageTypes.entries.first()
            Platform.get().logger.warning("Tried to get a messaging service of type $type, but it was not initialized. falling back to ${fallbackMethod} database method.")
            fallbackService
        }
    }

    fun service(categoryType: MessagingCategoryType): MessagingService {
        return service(categoryType.type)
    }

    private fun logMessagingConnectionError() {
        Platform.get().logger.severe("Connection to messaging service failed. Disabling the plugin...")
        Platform.get().logger.severe("Please check the following:")
        Platform.get().logger.severe("- Make sure your messaging service server is not misconfigured.")
        Platform.get().logger.severe("- Make sure your messaging service server is running.")
        Platform.get().logger.severe("Your active messaging services:")
        for (service in messageTypes) {
            Platform.get().logger.severe("- ${service.key.name} (${service.value::class.simpleName})")
        }
        Platform.get().logger.severe("Here's the full error trace:")
    }
}