package org.sayandev.sayanvanish.api.storage

import org.sayandev.sayanvanish.api.Platform
import org.sayandev.sayanvanish.api.storage.redis.RedisConfig
import org.sayandev.sayanvanish.api.storage.sql.SQLConfig
import org.sayandev.sayanvanish.api.storage.websocket.WebSocketConfig
import org.sayandev.stickynote.core.configuration.Config
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import java.io.File

public var storageConfig = StorageConfig.fromConfig() ?: StorageConfig.defaultConfig()

@ConfigSerializable
class StorageConfig(
    val transactionThreadCount: Int = 5,
    @Comment("Configuration for the database, including method, SQL, Redis, and caching options.")
    val method: DatabaseType = DatabaseType.SQL,
    @Comment("Configuration for SQL database")
    val sql: SQLConfig = SQLConfig(),
    @Comment("Configuration for Redis database")
    val redis: RedisConfig = RedisConfig(),
    val transactionTypes: MutableList<TransactionType> = TransactionTypes.entries.toMutableList(),
) : Config(Platform.get().rootDirectory, FILE_NAME, serializers()) {

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

    companion object {
        private const val FILE_NAME = "storage.yml"

        @JvmStatic
        fun defaultConfig(): StorageConfig {
            return StorageConfig().also { it.save() }
        }

        @JvmStatic
        fun fromConfig(): StorageConfig? {
            return fromConfig<StorageConfig>(File(Platform.get().rootDirectory, FILE_NAME), serializers())
        }

        fun serializers(): TypeSerializerCollection {
            return TypeSerializerCollection.builder()
                .register(TransactionType::class.java, TransactionType.Serializer)
                .build()
        }
    }
}