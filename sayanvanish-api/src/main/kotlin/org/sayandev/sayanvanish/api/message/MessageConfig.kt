package org.sayandev.sayanvanish.api.message

import org.sayandev.sayanvanish.api.Platform
import org.sayandev.sayanvanish.api.storage.redis.RedisConfig
import org.sayandev.sayanvanish.api.storage.websocket.WebSocketConfig
import org.sayandev.stickynote.core.configuration.Config
import kotlinx.serialization.Serializable
import com.charleskorn.kaml.YamlComment
import org.sayandev.sayanvanish.api.feature.Feature.Companion.directory
import java.io.File

// TODO: use a singleton or something, i don't want global scope variables like this anymore
public var messageConfig = MessageConfig.fromConfig() ?: MessageConfig.defaultConfig()

@Serializable
class MessageConfig(
    @YamlComment("Configuration for Redis database")
    val threadCount: Int = 5,
    val redis: RedisConfig = RedisConfig(),
    val webSocketConfig: WebSocketConfig = WebSocketConfig(),
    val categoryTypes: List<MessagingCategoryType> = MessagingCategoryTypes.entries,
) {

    fun save() {
        Config.save(File(Platform.get().rootDirectory, FILE_NAME), this)
    }

    companion object {
        private const val FILE_NAME = "message.yml"

        @JvmStatic
        fun defaultConfig(): MessageConfig {
            return MessageConfig().also { it.save() }
        }

        @JvmStatic
        fun fromConfig(): MessageConfig? {
            return Config.fromFile<MessageConfig>(File(Platform.get().rootDirectory, FILE_NAME))
        }
    }
}