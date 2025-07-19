package org.sayandev.sayanvanish.api.storage.redis

import kotlinx.serialization.Serializable
import com.charleskorn.kaml.YamlComment

@Serializable
data class RedisConfig(
    val threadCount: Int = 5,
    @YamlComment("The type of Redis configuration. Available types: STANDALONE")
    val type: RedisType = RedisType.STANDALONE,
    @YamlComment("Configuration for standalone Redis setup")
    val standalone: Standalone = Standalone(),
) {

    @Serializable
    data class Standalone(
        @YamlComment("The host address of the Redis database. If it's an IP address (x.x.x.x), ensure it is enclosed in double quotes (`\"`).")
        val host: String = "127.0.0.1",
        @YamlComment("The port number of the Redis server")
        val port: Int = 6379,
        @YamlComment("The username for accessing the Redis server")
        val user: String = "",
        @YamlComment("The password for accessing the Redis server")
        val password: String = "",
        @YamlComment("Whether to use SSL for the connection")
        val ssl: Boolean = false
    )

    @Serializable
    enum class RedisType {
        STANDALONE
    }
}