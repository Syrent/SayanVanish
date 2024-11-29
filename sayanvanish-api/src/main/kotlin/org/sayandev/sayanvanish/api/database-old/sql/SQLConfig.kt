package org.sayandev.sayanvanish.api.`database-old`.sql

import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting

@ConfigSerializable
data class SQLConfig(
    val method: SQLMethod = SQLMethod.SQLITE,
    val host: String = "localhost",
    val port: Int = 3306,
    val database: String = "sayanvanish",
    val username: String = "minecraft",
    val password: String = "",
    val poolProperties: PoolProperties = PoolProperties(),
    val tablePrefix: String = "sayanvanish_",
) {
    @ConfigSerializable
    data class PoolProperties(
        val maximumPoolSize: Int = 5,
        val minimumIdle: Int = 5,
        val maximumLifetime: Long = 1800000,
        val keepaliveTime: Long = 0,
        val connectionTimeout: Long = 5000,
        @Setting("use-ssl") val useSSL: Boolean = false,
        val verifyServerCertificate: Boolean = false,
        val allowPublicKeyRetrieval: Boolean = true,
    )

    enum class SQLMethod {
        MYSQL,
        SQLITE
    }
}