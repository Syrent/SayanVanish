package org.sayandev.sayanvanish.api.database.redis

import org.sayandev.sayanvanish.api.BasicUser
import org.sayandev.sayanvanish.api.User
import org.sayandev.sayanvanish.api.User.Companion.cast
import org.sayandev.sayanvanish.api.database.Database
import redis.clients.jedis.*
import java.util.*
import java.util.function.Consumer
import kotlin.reflect.KClass
import kotlin.reflect.safeCast


class RedisDatabase<U : User>(
    val config: RedisConfig
) : Database<U> {

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
        redis.set("test", "test1")
    }

    override fun connect() {
        /*redis.resource.use {
            if (config.standalone.password.isNotEmpty()) {
                if (config.standalone.user.isNotEmpty()) {
                    it.auth(config.standalone.user, config.standalone.password)
                } else {
                    it.auth(config.standalone.password)
                }
            }
            it.connect()
        }*/
    }

    override fun disconnect() {
        redis.close()
    }

    override fun getUser(uniqueId: UUID, useCache: Boolean, type: KClass<out User>): U? {
        val user = redis.hget("users", uniqueId.toString())
        return if (user != null) {
            val user = User.fromJson(user)
            val typedUser = (type.safeCast(user) as? U) ?: (user.cast(type) as U)
            typedUser
        } else {
            null
        }
    }

    override fun getUsers(useCache: Boolean): List<U> {
        val users = redis.hgetAll("users")
        return getUsers(useCache, User::class)
    }

    override fun getUsers(useCache: Boolean, type: KClass<out User>): List<U> {
        val users = redis.hgetAll("users")
        return users.map {
            val user = User.fromJson(it.value)
            (type.safeCast(user) as? U) ?: (user.cast(type) as U)
        }
    }

    override fun getBasicUsers(useCache: Boolean): List<BasicUser> {
        val users = redis.hgetAll("basic_users")
        return users.map { BasicUser.fromJson(it.value) }
    }

    override fun getUser(uniqueId: UUID, useCache: Boolean): U? {
        return getUser(uniqueId, useCache, User::class)
    }

    override fun addUser(user: U) {
        redis.hset("users", user.uniqueId.toString(), user.toJson())
    }

    override fun addBasicUser(user: BasicUser) {
        redis.hset("basic_users", user.uniqueId.toString(), user.toJson())
    }

    override fun hasUser(uniqueId: UUID, useCache: Boolean): Boolean {
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

    override fun isInQueue(uniqueId: UUID, result: Consumer<Boolean>) {
        redis.get("queue:$uniqueId")?.let { result.accept(true) } ?: result.accept(false)
    }

    override fun addToQueue(uniqueId: UUID, vanished: Boolean) {
        redis.set("queue:$uniqueId", vanished.toString())
    }

    override fun removeFromQueue(uniqueId: UUID) {
        redis.del("queue:$uniqueId")
    }

    override fun getFromQueue(uniqueId: UUID, result: Consumer<Boolean>) {
        redis.get("queue:$uniqueId")?.let { result.accept(it.toBoolean()) } ?: result.accept(false)
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

    override fun updateBasicCache() {
    }

}
