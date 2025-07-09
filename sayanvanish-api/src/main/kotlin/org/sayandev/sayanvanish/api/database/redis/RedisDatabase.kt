package org.sayandev.sayanvanish.api.database.redis

import kotlinx.coroutines.*
import org.sayandev.sayanvanish.api.Platform
import org.sayandev.sayanvanish.api.User
import org.sayandev.sayanvanish.api.VanishUser
import org.sayandev.sayanvanish.api.database.Database
import org.sayandev.sayanvanish.api.database.DatabaseConfig
import org.sayandev.stickynote.core.coroutine.dispatcher.AsyncDispatcher
import redis.clients.jedis.JedisPool
import java.util.*

class RedisDatabase(
    val config: DatabaseConfig,
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
                        User.fromJson(it)
                    }
            }
        }
    }

    override suspend fun getUsers(): Deferred<List<User>> {
        return async {
            redis.resource.use {
                it
                    .hgetAll("users")
                    .map { User.fromJson(it.value) }
            }
        }
    }

    override suspend fun addVanishUser(user: VanishUser): Deferred<Boolean> {
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
                it.hset("users", user.uniqueId.toString(), user.toJson()) != 0L
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
        return addVanishUser(user)
    }

    override suspend fun updateUser(user: User): Deferred<Boolean> {
        return saveUser(user)
    }

    override suspend fun isInQueue(uniqueId: UUID): Deferred<Boolean> {
        return async {
            redis.resource.use {
                it.get("queue:$uniqueId")?.toBoolean() ?: false
            }
        }
    }

    override suspend fun saveToQueue(uniqueId: UUID, vanished: Boolean): Deferred<Boolean> {
        return async {
            redis.resource.use {
                it.set("queue:$uniqueId", vanished.toString()) != null
            }
        }
    }

    override suspend fun removeFromQueue(uniqueId: UUID): Deferred<Boolean> {
        return async {
            redis.resource.use {
                it.del("queue:$uniqueId") != 0L
            }
        }
    }

    override suspend fun getFromQueue(uniqueId: UUID): Deferred<Boolean> {
        return async {
            redis.resource.use {
                it.get("queue:$uniqueId")?.toBoolean() ?: false
            }
        }
    }

    override suspend fun purgeAllTables(): Deferred<Boolean> {
        return async {
            redis.resource.use { it.del("vanish_users") }
            redis.resource.use { it.del("users") }
            redis.resource.use { it.del("queue") }
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
