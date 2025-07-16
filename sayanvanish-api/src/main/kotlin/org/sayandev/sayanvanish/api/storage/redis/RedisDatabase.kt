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
                        VanishUser.fromJson(it)
                    }
            }
        }
    }

    override suspend fun getVanishUsers(): Deferred<List<VanishUser>> {
        return async {
            redis.resource.use {
                it.hgetAll("vanish_users")
                    .map {
                        VanishUser.fromJson(it.value)
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
                it.hset("vanish_users", user.uniqueId.toString(), user.toJson()) != 0L
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
                it.hset("users", user.uniqueId.toString(), Gson.get().toJson(user)) != 0L
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

}
