package org.sayandev.sayanvanish.api.database.sql

import org.sayandev.stickynote.lib.spongepowered.configurate.objectmapping.ConfigSerializable
import org.sayandev.stickynote.lib.spongepowered.configurate.objectmapping.meta.Setting

@ConfigSerializable
data class SQLConfig(
    val method: SQLMethod = SQLMethod.SQLITE,
    val host: String = "localhost",
    val port: Int = 3306,
    val database: String = "root",
    val username: String = "minecraft",
    val password: String = "",
    val poolProperties: PoolProperties = PoolProperties(),
    val tablePrefix: String = "sayanvanish_",
) {
    @ConfigSerializable
    data class PoolProperties(
        val maximumPoolSize: Int = 10,
        val minimumIdle: Int = 10,
        val maximumLifetime: Long = 1800000,
        val keepaliveTime: Long = 0,
        val connectionTimeout: Long = 5000,
        @Setting("use-ssl") val useSSL: Boolean = false
    )

    enum class SQLMethod {
        MYSQL,
        SQLITE
    }
}