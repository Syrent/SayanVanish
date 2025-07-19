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

public var storageConfig = StorageConfig.fromConfig() ?: StorageConfig.defaultConfig()

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
        val file = File(Platform.get().rootDirectory, FILE_NAME)

        @JvmStatic
        fun defaultConfig(): StorageConfig {
            return StorageConfig().also { it.save() }
        }

        @JvmStatic
        fun fromConfig(): StorageConfig? {
            return Config.fromFile<StorageConfig>(File(Platform.get().rootDirectory, FILE_NAME))
        }
    }
}