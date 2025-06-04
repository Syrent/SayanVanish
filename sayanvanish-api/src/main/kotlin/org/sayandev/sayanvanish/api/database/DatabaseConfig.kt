package org.sayandev.sayanvanish.api.database

import org.sayandev.sayanvanish.api.Platform
import org.sayandev.sayanvanish.api.database.redis.RedisConfig
import org.sayandev.sayanvanish.api.database.sql.SQLConfig
import org.sayandev.stickynote.core.configuration.Config
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment
import org.spongepowered.configurate.serialize.TypeSerializer
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import java.io.File

public var databaseConfig = DatabaseConfig.fromConfig() ?: DatabaseConfig.defaultConfig()

@ConfigSerializable
class DatabaseConfig(
    val sqlDispatcherThreadCount: Int = 5,
    val redisDispatcherThreadCount: Int = 5,
    @Comment("Configuration for the database, including method, SQL, Redis, and caching options.")
    val method: DatabaseMethod = DatabaseMethod.SQL,
    @Comment("Configuration for SQL database")
    val sql: SQLConfig = SQLConfig(),
    @Comment("Configuration for Redis database")
    val redis: RedisConfig = RedisConfig(),
    val transactionTypes: List<TransactionType> = TransactionTypes.entries,
) : Config(Platform.get().rootDirectory, fileName, serializers()) {
    companion object {
        private val fileName = "database.yml"

        @JvmStatic
        fun defaultConfig(): DatabaseConfig {
            return DatabaseConfig().also { it.save() }
        }

        @JvmStatic
        fun fromConfig(): DatabaseConfig? {
            return fromConfig<DatabaseConfig>(File(Platform.get().rootDirectory, fileName), serializers())
        }

        fun serializers(): TypeSerializerCollection {
            return TypeSerializerCollection.builder()
                .register(TransactionType::class.java, TransactionType.Serializer)
                .build()
        }
    }
}