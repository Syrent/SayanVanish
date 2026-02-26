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

import org.sayandev.sayanvanish.api.Platform
import org.sayandev.sayanvanish.api.storage.redis.RedisConfig
import org.sayandev.sayanvanish.api.storage.websocket.WebSocketConfig
import org.sayandev.stickynote.core.configuration.Config
import kotlinx.serialization.Serializable
import com.charleskorn.kaml.YamlComment
import org.sayandev.sayanvanish.api.feature.Feature.Companion.directory
import java.io.File

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

        @Volatile
        private var config: MessageConfig = fromConfig() ?: defaultConfig()

        @JvmStatic
        fun get(): MessageConfig {
            return config
        }

        @JvmStatic
        fun defaultConfig(): MessageConfig {
            return MessageConfig().also { it.save() }
        }

        @JvmStatic
        fun fromConfig(): MessageConfig? {
            return Config.fromFile<MessageConfig>(File(Platform.get().rootDirectory, FILE_NAME))
        }

        @JvmStatic
        fun reload() {
            config = fromConfig() ?: defaultConfig()
        }

        @JvmStatic
        fun set(config: MessageConfig) {
            this.config = config
        }
    }
}
