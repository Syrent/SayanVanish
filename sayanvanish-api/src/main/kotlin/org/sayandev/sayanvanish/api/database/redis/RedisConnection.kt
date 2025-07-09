package org.sayandev.sayanvanish.api.database.redis

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