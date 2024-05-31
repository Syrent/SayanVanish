package org.sayandev.sayanvanish.api.database

import org.sayandev.sayanvanish.api.Platform
import org.sayandev.sayanvanish.api.database.redis.RedisConfig
import org.sayandev.sayanvanish.api.database.sql.SQLConfig
import org.sayandev.stickynote.core.configuration.Config
import org.sayandev.stickynote.lib.spongepowered.configurate.objectmapping.ConfigSerializable
import java.io.File

public var databaseConfig = DatabaseConfig.fromConfig() ?: DatabaseConfig.defaultConfig()

@ConfigSerializable
data class DatabaseConfig(
    val method: DatabaseMethod = DatabaseMethod.SQL,
    val sql: SQLConfig = SQLConfig(),
    val redis: RedisConfig = RedisConfig(),
    val useCacheWhenAvailable: Boolean = true,
) : Config(Platform.get().rootDirectory, fileName) {
    init {
        load()
    }

    companion object {
        private val fileName = "database.yml"

        @JvmStatic
        fun defaultConfig(): DatabaseConfig {
            return DatabaseConfig()
        }

        @JvmStatic
        fun fromConfig(): DatabaseConfig? {
            return fromConfig<DatabaseConfig>(File(Platform.get().rootDirectory, fileName))
        }
    }

}