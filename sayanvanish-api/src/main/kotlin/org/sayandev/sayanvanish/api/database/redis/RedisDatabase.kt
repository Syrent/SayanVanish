package org.sayandev.sayanvanish.api.database.redis

import kotlinx.coroutines.*
import org.sayandev.sayanvanish.api.Platform
import org.sayandev.sayanvanish.api.User
import org.sayandev.sayanvanish.api.VanishUser
import org.sayandev.sayanvanish.api.database.Database
import org.sayandev.sayanvanish.api.database.DatabaseConfig
import org.sayandev.stickynote.core.coroutine.dispatcher.AsyncDispatcher
import redis.clients.jedis.DefaultJedisClientConfig
import redis.clients.jedis.HostAndPort
import redis.clients.jedis.JedisPooled
import java.util.*

class RedisDatabase(
    val config: DatabaseConfig,
) : Database {

    override val dispatcher =
        AsyncDispatcher(
            "${Platform.get().pluginName.lowercase()}-redis-thread",
            config.redisDispatcherThreadCount,
        )

    lateinit var redis: JedisPooled

    override suspend fun initialize(): Deferred<Boolean> {
        redis = when (config.redis.type) {
            RedisConfig.RedisType.STANDALONE -> {
                val address = HostAndPort(config.redis.standalone.host, config.redis.standalone.port)
                JedisPooled(address, DefaultJedisClientConfig.builder().apply {
                    if (config.redis.standalone.user.isNotEmpty()) {
                        user(config.redis.standalone.user)
                    }
                    if (config.redis.standalone.password.isNotEmpty()) {
                        password(config.redis.standalone.password)
                    }
                    if (config.redis.standalone.ssl) {
                        ssl(config.redis.standalone.ssl)
                    }
                }.build())
            }
        }
        return CompletableDeferred(true)
    }

    override suspend fun connect(): Deferred<Boolean> {
        return CompletableDeferred(true)
    }

    override suspend fun disconnect(): Deferred<Boolean> {
        redis.close()
        return CompletableDeferred(true)
    }

    override suspend fun getVanishUser(uniqueId: UUID): Deferred<VanishUser?> {
        return async {
            redis
                .hget("vanish_users", uniqueId.toString())
                ?.let {
                    VanishUser.fromJson(it)
                }
        }
    }

    override suspend fun getVanishUsers(): Deferred<List<VanishUser>> {
        return async {
            redis
                .hgetAll("vanish_users")
                .map {
                    VanishUser.fromJson(it.value)
                }
        }
    }

    override suspend fun getUser(uniqueId: UUID): Deferred<User?> {
        return async {
            redis
                .hget("users", uniqueId.toString())
                ?.let {
                    User.fromJson(it)
                }
        }
    }

    override suspend fun getUsers(): Deferred<List<User>> {
        return async {
            redis
                .hgetAll("users")
                .map { User.fromJson(it.value) }
        }
    }

    override suspend fun addVanishUser(user: VanishUser): Deferred<Boolean> {
        return async {
            redis.hset("vanish_users", user.uniqueId.toString(), user.toJson()) != 0L
        }
    }

    override suspend fun hasVanishUser(uniqueId: UUID): Deferred<Boolean> {
        return async {
            redis.hexists("vanish_users", uniqueId.toString())
        }
    }

    override suspend fun addUser(user: User): Deferred<Boolean> {
        return async {
            redis.hset("users", user.uniqueId.toString(), user.toJson()) != 0L
        }
    }

    override suspend fun hasUser(uniqueId: UUID): Deferred<Boolean> {
        return async {
            redis.hexists("users", uniqueId.toString())
        }
    }

    override suspend fun removeVanishUser(uniqueId: UUID): Deferred<Boolean> {
        return async {
            redis.hdel("vanish_users", uniqueId.toString()) != 0L
        }
    }

    override suspend fun removeUser(uniqueId: UUID): Deferred<Boolean> {
        return async {
            redis.hdel("users", uniqueId.toString()) != 0L
        }
    }

    override suspend fun updateVanishUser(user: VanishUser): Deferred<Boolean> {
        return addVanishUser(user)
    }

    override suspend fun updateUser(user: User): Deferred<Boolean> {
        return addUser(user)
    }

    override suspend fun isInQueue(uniqueId: UUID): Deferred<Boolean> {
        return async {
            redis.get("queue:$uniqueId")?.toBoolean() ?: false
        }
    }

    override suspend fun addToQueue(uniqueId: UUID, vanished: Boolean): Deferred<Boolean> {
        return async {
            redis.set("queue:$uniqueId", vanished.toString()) != null
        }
    }

    override suspend fun removeFromQueue(uniqueId: UUID): Deferred<Boolean> {
        return async {
            redis.del("queue:$uniqueId") != 0L
        }
    }

    override suspend fun getFromQueue(uniqueId: UUID): Deferred<Boolean> {
        return async {
            redis.get("queue:$uniqueId")?.toBoolean() ?: false
        }
    }

    override suspend fun purgeAllTables(): Deferred<Boolean> {
        return async {
            redis.del("vanish_users")
            redis.del("users")
            redis.del("queue")
            true
        }
    }

    override suspend fun purgeUsers(): Deferred<Boolean> {
        return async {
            redis.del("users")
            true
        }
    }

    override suspend fun purgeUsers(serverId: String): Deferred<Boolean> {
        return async {
            // TODO: only remove users from this server id
            redis.del("users")
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
