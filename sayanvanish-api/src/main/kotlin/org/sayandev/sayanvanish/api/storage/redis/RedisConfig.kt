/*
 * This file is part of SayanVanish, licensed under the GNU General Public License v3.0.
 *
 * Copyright (c) 2026 Sayan Development and contributors
 *
 * SayanVanish is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SayanVanish is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
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