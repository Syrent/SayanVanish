package org.sayandev.sayanvanish.api.database.redis

import org.sayandev.sayanvanish.api.BasicUser
import org.sayandev.sayanvanish.api.User
import org.sayandev.sayanvanish.api.User.Companion.cast
import org.sayandev.sayanvanish.api.database.Database
import org.sayandev.stickynote.lib.jedis.clients.jedis.DefaultJedisClientConfig
import org.sayandev.stickynote.lib.jedis.clients.jedis.HostAndPort
import org.sayandev.stickynote.lib.jedis.clients.jedis.JedisPooled
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

    override fun addUserAsync(user: U, result: () -> Unit) {
        Thread {
            addUser(user)
            result()
        }.start()
    }

    override fun getUser(uniqueId: UUID, type: KClass<out User>): U? {
        val user = redis.hget("users", uniqueId.toString())
        return if (user != null) {
            val user = User.fromJson(user)
            val typedUser = (type.safeCast(user) as? U) ?: (user.cast(type) as U)
            typedUser
        } else {
            null
        }
    }

    override fun getUsers(): List<U> {
        val users = redis.hgetAll("users")
        return getUsers(User::class)
    }

    override fun getUsersAsync(result: (List<U>) -> Unit) {
        getUsersAsync(User::class, result)
    }

    override fun getUsersAsync(type: KClass<out User>, result: (List<U>) -> Unit) {
        Thread {
            val users = getUsers(type)
            result(users)
        }.start()
    }

    override fun getUsers(type: KClass<out User>): List<U> {
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

    override fun getBasicUsersAsync(result: (List<BasicUser>) -> Unit) {
        Thread {
            val users = getBasicUsers(true)
            result(users)
        }.start()
    }

    override fun getUser(uniqueId: UUID): U? {
        return getUser(uniqueId, User::class)
    }

    override fun getUserAsync(uniqueId: UUID, result: (U?) -> Unit) {
        getUserAsync(uniqueId, User::class, result)
    }

    override fun getUserAsync(uniqueId: UUID, type: KClass<out User>, result: (U?) -> Unit) {
        Thread {
            val user = getUser(uniqueId, type)
            result(user)
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

    override fun hasUserAsync(uniqueId: UUID, result: (Boolean) -> Unit) {
        Thread {
            result(hasUser(uniqueId))
        }.start()
    }

    override fun updateUserAsync(user: U, result: () -> Unit) {
        Thread {
            updateUser(user)
            result()
        }.start()
    }

    override fun hasBasicUser(uniqueId: UUID, useCache: Boolean): Boolean {
        return redis.hexists("basic_users", uniqueId.toString())
    }

    override fun removeUser(uniqueId: UUID) {
        redis.hdel("users", uniqueId.toString())
    }

    override fun removeUserAsync(uniqueId: UUID, result: () -> Unit) {
        Thread {
            removeUser(uniqueId)
            result()
        }.start()
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

    override fun updateBasicCache() {
    }

}
