package org.sayandev.sayanvanish.api.storage.sql

import kotlinx.serialization.Serializable
import com.charleskorn.kaml.YamlComment
import org.spongepowered.configurate.objectmapping.meta.Setting

@Serializable
class SQLConfig(
    val threadCount: Int = 5,
    @YamlComment("The method to use for the database. Available methods: MYSQL, SQLITE")
    val method: SQLMethod = SQLMethod.SQLITE,
    @YamlComment("The host address of the SQL database. If it's an IP address (x.x.x.x), ensure it is enclosed in double quotes (`\"`).")
    val host: String = "127.0.0.1",
    @YamlComment("The port number of the SQL database")
    val port: Int = 3306,
    @YamlComment("The name of the SQL database")
    val database: String = "sayanvanish",
    @YamlComment("The username for accessing the SQL database")
    val username: String = "minecraft",
    @YamlComment("The password for accessing the SQL database")
    val password: String = "",
    @YamlComment("Properties for the connection pool")
    val poolProperties: PoolProperties = PoolProperties(),
    @YamlComment("The prefix for the table names in the SQL database")
    // TODO: use this?
    val tablePrefix: String = "sayanvanish_",
) {
    @Serializable
    data class PoolProperties(
        @YamlComment("The maximum number of connections in the pool")
        val maximumPoolSize: Int = 5,
        @YamlComment("The minimum number of idle connections in the pool")
        val minimumIdle: Int = 5,
        @YamlComment("The maximum lifetime of a connection in the pool (in milliseconds)")
        val maximumLifetime: Long = 1800000,
        @YamlComment("The keepalive time for a connection in the pool (in milliseconds)")
        val keepaliveTime: Long = 0,
        @YamlComment("The connection timeout (in milliseconds)")
        val connectionTimeout: Long = 5000,
        @YamlComment("Whether to use SSL for the connection")
        @Setting("use-ssl") val useSSL: Boolean = false,
        @YamlComment("Whether to verify the server certificate")
        val verifyServerCertificate: Boolean = false,
        @YamlComment("Whether to allow public key retrieval")
        val allowPublicKeyRetrieval: Boolean = true,
        val maxLifetime: Long = 1800000,
    )

    @Serializable
    enum class SQLMethod {
        MYSQL,
        MARIADB,
        SQLITE
    }
}