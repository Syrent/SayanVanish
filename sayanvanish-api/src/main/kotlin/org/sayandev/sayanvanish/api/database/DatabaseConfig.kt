package org.sayandev.sayanvanish.api.database

import org.sayandev.sayanvanish.api.Platform
import org.sayandev.sayanvanish.api.database.redis.RedisConfig
import org.sayandev.sayanvanish.api.database.sql.SQLConfig
import org.sayandev.stickynote.core.configuration.Config
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment
import java.io.File

public var databaseConfig = DatabaseConfig.fromConfig() ?: DatabaseConfig.defaultConfig()

@ConfigSerializable
data class DatabaseConfig(
    @Comment("Configuration for the database, including method, SQL, Redis, and caching options.")
    val method: DatabaseMethod = DatabaseMethod.SQL,
    @Comment("Configuration for SQL database")
    val sql: SQLConfig = SQLConfig(),
    @Comment("Configuration for Redis database")
    val redis: RedisConfig = RedisConfig(),
    @Comment("Whether to use cache when available")
    val useCacheWhenAvailable: Boolean = true,
) : Config(Platform.get().rootDirectory, fileName) {
    companion object {
        private val fileName = "database.yml"

        @JvmStatic
        fun defaultConfig(): DatabaseConfig {
            return DatabaseConfig().also { it.save() }
        }

        @JvmStatic
        fun fromConfig(): DatabaseConfig? {
            return fromConfig<DatabaseConfig>(File(Platform.get().rootDirectory, fileName))
        }
    }
}