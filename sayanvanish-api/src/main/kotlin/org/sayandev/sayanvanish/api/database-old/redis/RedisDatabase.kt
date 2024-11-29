package org.sayandev.sayanvanish.api.`database-old`.redis

import org.sayandev.sayanvanish.api.BasicUser
import org.sayandev.sayanvanish.api.User
import org.sayandev.sayanvanish.api.User.Companion.convert
import org.sayandev.sayanvanish.api.`database-old`.Database
import redis.clients.jedis.DefaultJedisClientConfig
import redis.clients.jedis.HostAndPort
import redis.clients.jedis.JedisPooled
import java.util.*
import kotlin.reflect.safeCast


class RedisDatabase<U : User>(
    val config: RedisConfig,
    val type: Class<out User>
) : Database<U> {

    override var cache = mutableMapOf<UUID, U>()
    override var useCache = false

    lateinit var redis: JedisPooled

    override fun initialize() {
        redis = when (config.type) {
            RedisConfig.RedisType.STANDALONE -> {
                val address = HostAndPort(config.standalone.host, config.standalone.port)
                val config = DefaultJedisClientConfig.builder().apply {
                    if (config.standalone.user.isNotEmpty()) {
                        user(config.standalone.user)
                    }
                    if (config.standalone.password.isNotEmpty()) {
                        password(config.standalone.password)
                    }
                    if (config.standalone.ssl) {
                        ssl(config.standalone.ssl)
                    }
                }.build()
                JedisPooled(address, config)
            }
        }
    }

    override fun connect() {
    }

    override fun disconnect() {
        redis.close()
    }

    override fun getUser(uniqueId: UUID): U? {
        val user = redis.hget("users", uniqueId.toString())
        return if (user != null) {
            val user = User.fromJson(user)
            val typedUser = (type.kotlin.safeCast(user) as? U) ?: (user.convert(type) as U)
            typedUser
        } else {
            null
        }
    }

    override fun getUsersAsync(result: (List<U>) -> Unit) {
        Thread {
            val users = getUsers()
            result(users)
        }.start()
    }

    override fun getUsers(): List<U> {
        val users = redis.hgetAll("users")
        return users.map {
            val user = User.fromJson(it.value)
            (type.kotlin.safeCast(user) as? U) ?: (user.convert(type) as U)
        }
    }

    override fun getBasicUsers(useCache: Boolean): List<BasicUser> {
        val users = redis.hgetAll("basic_users")
        return users.map { BasicUser.fromJson(it.value) }
    }

    override fun getBasicUsersAsync(result: (List<BasicUser>) -> Unit) {
        Thread {
            val users = getBasicUsers(true)
            result(users)
        }.start()
    }

    override fun addUser(user: U) {
        redis.hset("users", user.uniqueId.toString(), user.toJson())
    }

    override fun addBasicUser(user: BasicUser) {
        redis.hset("basic_users", user.uniqueId.toString(), user.toJson())
    }

    override fun hasUser(uniqueId: UUID): Boolean {
        return redis.hexists("users", uniqueId.toString())
    }

    override fun hasBasicUser(uniqueId: UUID, useCache: Boolean): Boolean {
        return redis.hexists("basic_users", uniqueId.toString())
    }

    override fun removeUser(uniqueId: UUID) {
        redis.hdel("users", uniqueId.toString())
    }

    override fun removeBasicUser(uniqueId: UUID) {
        redis.hdel("basic_users", uniqueId.toString())
    }

    override fun updateUser(user: U) {
        addUser(user)
    }

    override fun updateBasicUser(user: BasicUser) {
        addBasicUser(user)
    }

    override fun isInQueue(uniqueId: UUID, result: (Boolean) -> Unit) {
        redis.get("queue:$uniqueId")?.let { result(true) } ?: result(false)
    }

    override fun addToQueue(uniqueId: UUID, vanished: Boolean) {
        redis.set("queue:$uniqueId", vanished.toString())
    }

    override fun removeFromQueue(uniqueId: UUID) {
        redis.del("queue:$uniqueId")
    }

    override fun getFromQueue(uniqueId: UUID, result: (Boolean) -> Unit) {
        redis.get("queue:$uniqueId")?.let { result(it.toBoolean()) } ?: result(false)
    }

    override fun purgeCache() {
    }

    override fun purge() {
    }

    override fun purgeBasic() {
        redis.del("basic_users")
    }

    override fun purgeBasic(serverId: String) {
        purgeBasic()
    }

    override fun updateCacheAsync() {
    }

    override fun updateBasicCacheAsync() {
    }

}
