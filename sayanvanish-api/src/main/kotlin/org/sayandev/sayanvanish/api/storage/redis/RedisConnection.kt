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

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import org.sayandev.stickynote.core.coroutine.dispatcher.AsyncDispatcher
import redis.clients.jedis.DefaultJedisClientConfig
import redis.clients.jedis.HostAndPort
import redis.clients.jedis.JedisPool

class RedisConnection(
    val config: RedisConfig,
    val dispatcher: AsyncDispatcher,
) {
    var connected = false

    lateinit var redis: JedisPool

    suspend fun initialize(): Deferred<Boolean> {
        redis = when (config.type) {
            RedisConfig.RedisType.STANDALONE -> {
                val address = HostAndPort(config.standalone.host, config.standalone.port)
                JedisPool(address, DefaultJedisClientConfig.builder().apply {
                    if (config.standalone.user.isNotEmpty()) {
                        user(config.standalone.user)
                    }
                    if (config.standalone.password.isNotEmpty()) {
                        password(config.standalone.password)
                    }
                    if (config.standalone.ssl) {
                        ssl(config.standalone.ssl)
                    }
                }.build())
            }
        }
        connected = true
        return CompletableDeferred(true)
    }

    fun connect(): Deferred<Boolean> {
        return CompletableDeferred(true)
    }

    fun disconnect(): Deferred<Boolean> {
        redis.close()
        return CompletableDeferred(true)
    }
}