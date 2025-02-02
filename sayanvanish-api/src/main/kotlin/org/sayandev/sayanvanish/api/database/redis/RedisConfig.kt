package org.sayandev.sayanvanish.api.database.redis

import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment

@ConfigSerializable
data class RedisConfig(
    @Comment("The type of Redis configuration. Available types: STANDALONE")
    val type: RedisType = RedisType.STANDALONE,
    @Comment("Configuration for standalone Redis setup")
    val standalone: Standalone = Standalone(),
) {

    @ConfigSerializable
    data class Standalone(
        @Comment("The host address of the Redis database. If it's an IP address (x.x.x.x), ensure it is enclosed in double quotes (`\"`).")
        val host: String = "localhost",
        @Comment("The port number of the Redis server")
        val port: Int = 6379,
        @Comment("The username for accessing the Redis server")
        val user: String = "",
        @Comment("The password for accessing the Redis server")
        val password: String = "",
        @Comment("Whether to use SSL for the connection")
        val ssl: Boolean = false
    )

    enum class RedisType {
        STANDALONE
    }
}