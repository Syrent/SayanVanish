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

import com.google.gson.JsonParser
import kotlinx.coroutines.*
import org.sayandev.sayanvanish.api.Platform
import org.sayandev.sayanvanish.api.User
import org.sayandev.sayanvanish.api.VanishUser
import org.sayandev.sayanvanish.api.storage.Database
import org.sayandev.sayanvanish.api.storage.StorageConfig
import org.sayandev.sayanvanish.api.utils.Gson
import org.sayandev.stickynote.core.coroutine.dispatcher.AsyncDispatcher
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool
import java.util.*

class RedisDatabase(
    val config: StorageConfig,
) : Database {

    val connection = RedisConnection(config.redis, AsyncDispatcher(
        "${Platform.get().pluginName.lowercase()}-redis-thread",
        config.redis.threadCount
    ))

    override var connected = false

    override val dispatcher = connection.dispatcher

    lateinit var redis: JedisPool

    override suspend fun initialize(): Deferred<Boolean> {
        connection.initialize()
        redis = connection.redis
        connected = connection.connected
        return CompletableDeferred(connected)
    }

    override suspend fun connect(): Deferred<Boolean> {
        return CompletableDeferred(connected)
    }

    override suspend fun disconnect(): Deferred<Boolean> {
        redis.close()
        connected = false
        return CompletableDeferred(connected)
    }

    override suspend fun getVanishUser(uniqueId: UUID): Deferred<VanishUser?> {
        return async {
            redis.resource.use {
                it.hget("vanish_users", uniqueId.toString())
                    ?.let {
                        Gson.get().fromJson(JsonParser.parseString(it), VanishUser::class.java)
                    }
                    ?.adapt()
            }
        }
    }

    override suspend fun getVanishUsers(): Deferred<List<VanishUser>> {
        return async {
            redis.resource.use {
                it.hgetAll("vanish_users")
                    .map {
                        Gson.get().fromJson(JsonParser.parseString(it.value), VanishUser::class.java).adapt()
                    }
            }
        }
    }

    override suspend fun getUser(uniqueId: UUID): Deferred<User?> {
        return async {
            redis.resource.use {
                it
                    .hget("users", uniqueId.toString())
                    ?.let {
                        Gson.get().fromJson(JsonParser.parseString(it), User::class.java)
                    }
            }
        }
    }

    override suspend fun getUsers(): Deferred<List<User>> {
        return async {
            redis.resource.use {
                it
                    .hgetAll("users")
                    .map { Gson.get().fromJson(JsonParser.parseString(it.value), User::class.java) }
            }
        }
    }

    override suspend fun saveVanishUser(user: VanishUser): Deferred<Boolean> {
        return async {
            redis.resource.use {
                val resolvedServerId = resolveServerId(it, user.uniqueId, runCatching { user.serverId }.getOrNull())
                val payload = VanishUser.Generic(
                    uniqueId = user.uniqueId,
                    username = user.username,
                    serverId = resolvedServerId,
                    isVanished = user.isVanished,
                    isOnline = user.isOnline,
                    vanishLevel = user.vanishLevel,
                    currentOptions = user.currentOptions
                )
                it.hset("vanish_users", user.uniqueId.toString(), Gson.get().toJson(payload)) != 0L
            }
        }
    }

    override suspend fun hasVanishUser(uniqueId: UUID): Deferred<Boolean> {
        return async {
            redis.resource.use {
                it.hexists("vanish_users", uniqueId.toString())
            }
        }
    }

    override suspend fun saveUser(user: User): Deferred<Boolean> {
        return async {
            redis.resource.use {
                val resolvedServerId = resolveServerId(it, user.uniqueId, runCatching { user.serverId }.getOrNull())
                val payload = User.Generic(
                    uniqueId = user.uniqueId,
                    username = user.username,
                    isOnline = user.isOnline,
                    serverId = resolvedServerId
                )
                it.hset("users", user.uniqueId.toString(), Gson.get().toJson(payload)) != 0L
            }
        }
    }

    override suspend fun hasUser(uniqueId: UUID): Deferred<Boolean> {
        return async {
            redis.resource.use {
                it.hexists("users", uniqueId.toString())
            }
        }
    }

    override suspend fun removeVanishUser(uniqueId: UUID): Deferred<Boolean> {
        return async {
            redis.resource.use {
                it.hdel("vanish_users", uniqueId.toString()) != 0L
            }
        }
    }

    override suspend fun removeUser(uniqueId: UUID): Deferred<Boolean> {
        return async {
            redis.resource.use {
                it.hdel("users", uniqueId.toString()) != 0L
            }
        }
    }

    override suspend fun updateVanishUser(user: VanishUser): Deferred<Boolean> {
        return saveVanishUser(user)
    }

    override suspend fun updateUser(user: User): Deferred<Boolean> {
        return saveUser(user)
    }

    override suspend fun purgeAllTables(): Deferred<Boolean> {
        return async {
            redis.resource.use { it.del("vanish_users") }
            redis.resource.use { it.del("users") }
            true
        }
    }

    override suspend fun purgeUsers(): Deferred<Boolean> {
        return async {
            redis.resource.use { it.del("users") }
            true
        }
    }

    override suspend fun purgeUsers(serverId: String): Deferred<Boolean> {
        return async {
            // TODO: only remove users from this server id
            redis.resource.use { it.del("users") }
            true
        }
    }

    fun <T> async(
        block: suspend CoroutineScope.() -> T
    ): Deferred<T> {
        val session = CoroutineScope(dispatcher)
        if (!session.isActive) {
            return CompletableDeferred<T>().apply { cancel() }
        }

        return session.async(dispatcher, CoroutineStart.DEFAULT, block)
    }

    private fun resolveServerId(resource: Jedis, uniqueId: UUID, serverId: String?): String {
        return serverId
            ?: readServerId(resource, "users", uniqueId)
            ?: readServerId(resource, "vanish_users", uniqueId)
            ?: Platform.get().serverId
    }

    private fun readServerId(resource: Jedis, key: String, uniqueId: UUID): String? {
        return resource.hget(key, uniqueId.toString())
            ?.let { JsonParser.parseString(it).asJsonObject }
            ?.let { json ->
                if (json.has("serverId") && !json.get("serverId").isJsonNull) json.get("serverId").asString else null
            }
    }

}
