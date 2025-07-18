package org.sayandev.sayanvanish.api.message

import org.sayandev.sayanvanish.api.Platform
import org.sayandev.sayanvanish.api.storage.redis.RedisConfig
import org.sayandev.sayanvanish.api.storage.websocket.WebSocketConfig
import org.sayandev.stickynote.core.configuration.Config
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import java.io.File

// TODO: use a singleton or something, i don't want global scope variables like this anymore
public var messageConfig = MessageConfig.fromConfig() ?: MessageConfig.defaultConfig()

@ConfigSerializable
class MessageConfig(
    @Comment("Configuration for Redis database")
    val threadCount: Int = 5,
    val redis: RedisConfig = RedisConfig(),
    val webSocketConfig: WebSocketConfig = WebSocketConfig(),
    val categoryTypes: List<MessagingCategoryType> = MessagingCategoryTypes.entries,
) : Config(Platform.get().rootDirectory, fileName, serializers()) {
    companion object {
        private val fileName = "message.yml"

        @JvmStatic
        fun defaultConfig(): MessageConfig {
            return MessageConfig().also { it.save() }
        }

        @JvmStatic
        fun fromConfig(): MessageConfig? {
            return fromConfig<MessageConfig>(File(Platform.get().rootDirectory, fileName), serializers())
        }

        fun serializers(): TypeSerializerCollection {
            return TypeSerializerCollection.builder()
                .register(MessagingCategoryType::class.java, MessagingCategoryType.Serializer)
                .build()
        }
    }
}