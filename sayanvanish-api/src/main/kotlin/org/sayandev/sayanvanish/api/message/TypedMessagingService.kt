package org.sayandev.sayanvanish.api.message

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import org.sayandev.sayanvanish.api.Platform
import org.sayandev.sayanvanish.api.User
import org.sayandev.sayanvanish.api.VanishUser
import org.sayandev.sayanvanish.api.message.types.RedisMessagingService
import org.sayandev.sayanvanish.api.message.types.WebSocketMessagingService
import org.sayandev.sayanvanish.api.utils.Gson
import org.sayandev.stickynote.core.coroutine.dispatcher.AsyncDispatcher
import org.sayandev.stickynote.core.messaging.PayloadWrapper

class TypedMessagingService: MessagingService {
    override val dispatcher =
        AsyncDispatcher(
            "${Platform.get().pluginName.lowercase()}-messaging-thread",
            messageConfig.threadCount
        )

    val messageTypes = mutableMapOf<MessagingTypes, MessagingService>()
    var messagingConnected: Boolean = true
        private set
    var enabled: Boolean = true
        private set

    private val noOpMessagingService = object : MessagingService {
        override val dispatcher: AsyncDispatcher = this@TypedMessagingService.dispatcher

        override suspend fun syncUser(user: User): Deferred<Boolean> {
            return CompletableDeferred(true)
        }

        override suspend fun syncVanishUser(vanishUser: VanishUser): Deferred<Boolean> {
            return CompletableDeferred(true)
        }
    }

    suspend fun initialize(enabled: Boolean = true): Deferred<Boolean> {
        this.enabled = enabled
        if (!enabled) {
            disable()
            return CompletableDeferred(true)
        }
        if (messageTypes.isNotEmpty()) {
            return CompletableDeferred(true)
        }

        messagingConnected = true

        PayloadWrapper.registerSerializer(User::class.java, User.JsonAdapter())
        PayloadWrapper.registerDeserializer(User::class.java, User.JsonAdapter())
        PayloadWrapper.registerDeserializer(VanishUser::class.java, VanishUser.JsonAdapter())
        PayloadWrapper.registerDeserializer(VanishUser::class.java, VanishUser.JsonAdapter())

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

    suspend fun reload(enabled: Boolean): Deferred<Boolean> {
        if (!enabled) {
            disable()
            return CompletableDeferred(true)
        }

        if (this.enabled && messageTypes.isNotEmpty()) {
            return CompletableDeferred(true)
        }

        disable()
        return initialize(true)
    }

    inline fun <reified M: MessagingService> getByType(): M {
        return messageTypes.values.filterIsInstance<M>().firstOrNull()
            ?: throw IllegalArgumentException("Received database with type `${M::class.simpleName}` but it isn't registered in the TransactionDatabase database types.")
    }

    override suspend fun syncUser(user: User): Deferred<Boolean> {
        if (!enabled || messageTypes.isEmpty()) {
            return CompletableDeferred(true)
        }
        return messagingService(MessagingCategoryTypes.SYNC_USER).syncUser(user)
    }

    override suspend fun syncVanishUser(vanishUser: VanishUser): Deferred<Boolean> {
        if (!enabled || messageTypes.isEmpty()) {
            return CompletableDeferred(true)
        }
        return messagingService(MessagingCategoryTypes.SYNC_VANISH_USER).syncVanishUser(vanishUser)
    }

    fun messagingService(type: MessagingType): MessagingService {
        if (!enabled || messageTypes.isEmpty()) {
            return noOpMessagingService
        }
        return messageTypes[type] ?: let {
            val (fallbackMethod, fallbackService) = messageTypes.entries.first()
            Platform.get().logger.warning("Tried to get a messaging service of type $type, but it was not initialized. falling back to ${fallbackMethod} database method.")
            fallbackService
        }
    }

    fun messagingService(categoryType: MessagingCategoryType): MessagingService {
        return messagingService(categoryType.type)
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

    private suspend fun disable(): Deferred<Boolean> {
        for ((_, messagingService) in messageTypes) {
            when (messagingService) {
                is RedisMessagingService -> messagingService.shutdown().await()
                is WebSocketMessagingService -> messagingService.shutdown().await()
            }
        }

        messageTypes.clear()
        this.enabled = false
        this.messagingConnected = true
        return CompletableDeferred(true)
    }
}
