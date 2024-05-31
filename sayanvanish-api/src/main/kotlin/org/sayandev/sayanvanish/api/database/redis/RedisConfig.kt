package org.sayandev.sayanvanish.api.database.redis

import org.sayandev.stickynote.lib.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class RedisConfig(
    val type: RedisType = RedisType.STANDALONE,
    val standalone: Standalone = Standalone(),
) {

    @ConfigSerializable
    data class Standalone(
        val host: String = "localhost",
        val port: Int = 6379,
        val user: String = "",
        val password: String = "",
        val ssl: Boolean = false
    )

    enum class RedisType {
        STANDALONE
    }
}