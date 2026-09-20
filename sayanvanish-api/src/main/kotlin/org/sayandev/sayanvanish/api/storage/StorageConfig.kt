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
package org.sayandev.sayanvanish.api.storage

import org.sayandev.sayanvanish.api.Platform
import org.sayandev.sayanvanish.api.storage.redis.RedisConfig
import org.sayandev.sayanvanish.api.storage.sql.SQLConfig
import org.sayandev.sayanvanish.api.storage.websocket.WebSocketConfig
import org.sayandev.stickynote.core.configuration.Config
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import com.charleskorn.kaml.YamlComment
import org.sayandev.sayanvanish.api.feature.Feature.Companion.directory
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import java.io.File

@Serializable
class StorageConfig(
    val transactionThreadCount: Int = 5,
    @YamlComment("Configuration for the database, including method, SQL, Redis, and caching options.")
    val method: DatabaseType = DatabaseType.SQL,
    @YamlComment("Configuration for SQL database")
    val sql: SQLConfig = SQLConfig(),
    @YamlComment("Configuration for Redis database")
    val redis: RedisConfig = RedisConfig(),
    val transactionTypes: MutableList<TransactionType> = TransactionTypes.entries.toMutableList(),
) {

    init {
        // Make sure to add missing transaction types to configuration file
        var addedNewType = false
        for (missingType in TransactionTypes.entries.filter { !transactionTypes.contains(it) }) {
            transactionTypes.add(missingType)
            addedNewType = true
        }
        if (addedNewType) {
            save()
        }
    }

    fun save() {
        Config.save(file, this)
    }

    companion object {
        private const val FILE_NAME = "storage.yml"
        @JvmStatic
        val file = File(Platform.get().rootDirectory, FILE_NAME)

        @Volatile
        private var config: StorageConfig = fromConfig() ?: defaultConfig()

        @JvmStatic
        fun get(): StorageConfig {
            return config
        }

        @JvmStatic
        fun defaultConfig(): StorageConfig {
            return StorageConfig().also { it.save() }
        }

        @JvmStatic
        fun fromConfig(): StorageConfig? {
            return Config.fromFile<StorageConfig>(File(Platform.get().rootDirectory, FILE_NAME))
        }

        @JvmStatic
        fun reload(): StorageConfig {
            config = fromConfig() ?: defaultConfig()
            return config
        }

        @JvmStatic
        fun set(config: StorageConfig) {
            this.config = config
        }
    }
}
