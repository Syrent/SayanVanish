package org.sayandev.sayanvanish.api.database.sql

import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment
import org.spongepowered.configurate.objectmapping.meta.Setting

@ConfigSerializable
class SQLConfig(
    val threadCount: Int = 5,
    @Comment("The method to use for the database. Available methods: MYSQL, SQLITE")
    val method: SQLMethod = SQLMethod.SQLITE,
    @Comment("The host address of the SQL database. If it's an IP address (x.x.x.x), ensure it is enclosed in double quotes (`\"`).")
    val host: String = "127.0.0.1",
    @Comment("The port number of the SQL database")
    val port: Int = 3306,
    @Comment("The name of the SQL database")
    val database: String = "sayanvanish",
    @Comment("The username for accessing the SQL database")
    val username: String = "minecraft",
    @Comment("The password for accessing the SQL database")
    val password: String = "",
    @Comment("Properties for the connection pool")
    val poolProperties: PoolProperties = PoolProperties(),
    @Comment("The prefix for the table names in the SQL database")
    val tablePrefix: String = "sayanvanish_",
) {
    @ConfigSerializable
    data class PoolProperties(
        @Comment("The maximum number of connections in the pool")
        val maximumPoolSize: Int = 5,
        @Comment("The minimum number of idle connections in the pool")
        val minimumIdle: Int = 5,
        @Comment("The maximum lifetime of a connection in the pool (in milliseconds)")
        val maximumLifetime: Long = 1800000,
        @Comment("The keepalive time for a connection in the pool (in milliseconds)")
        val keepaliveTime: Long = 0,
        @Comment("The connection timeout (in milliseconds)")
        val connectionTimeout: Long = 5000,
        @Comment("Whether to use SSL for the connection")
        @Setting("use-ssl") val useSSL: Boolean = false,
        @Comment("Whether to verify the server certificate")
        val verifyServerCertificate: Boolean = false,
        @Comment("Whether to allow public key retrieval")
        val allowPublicKeyRetrieval: Boolean = true,
        val maxLifetime: Long = 1800000,
    )

    enum class SQLMethod {
        MYSQL,
        MARIADB,
        SQLITE
    }
}