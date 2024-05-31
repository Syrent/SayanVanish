package org.sayandev.sayanvanish.api

import org.sayandev.sayanvanish.api.database.DatabaseMethod
import org.sayandev.sayanvanish.api.database.sql.SQLDatabase
import org.sayandev.sayanvanish.api.database.databaseConfig
import org.sayandev.sayanvanish.api.database.redis.RedisDatabase
import java.util.*
import kotlin.reflect.KClass

open class SayanVanishAPI<U: User>(val type: KClass<out User>, val useCache: Boolean) {
    constructor(type: KClass<out User>): this(type, false)
    constructor(): this(User::class)

    val database = when (databaseConfig.method) {
        DatabaseMethod.SQL -> {
            SQLDatabase<U>(databaseConfig.sql).apply {
                this.connect()
                this.initialize()
            }
        }
        DatabaseMethod.REDIS -> {
            RedisDatabase<U>(databaseConfig.redis).apply {
                this.initialize()
                this.connect()
            }
        }
    }

    fun getPlatform(): Platform {
        return Platform.get()
    }

    fun getUsers(): List<U> {
        return database.getUsers(useCache, type)
    }

    fun getUsers(predicate: (U) -> Boolean): List<U> {
        return getUsers().filter(predicate)
    }

    fun getBasicUsers(): List<BasicUser> {
        return database.getBasicUsers(useCache)
    }

    fun getSortedUsers(predicate: (U) -> Int) {
        getUsers().sortedByDescending(predicate)
    }

    fun getVanishedUsers(): Collection<U> {
        return getUsers(User::isVanished)
    }

    fun getOnlineUsers(): Collection<U> {
        return getUsers(User::isOnline)
    }

    fun addUser(user: U) {
        database.addUser(user)
    }

    fun addBasicUser(user: BasicUser) {
        database.addBasicUser(user)
    }

    fun removeUser(uniqueId: UUID) {
        database.removeUser(uniqueId)
    }

    fun removeBasicUser(uniqueId: UUID) {
        database.removeBasicUser(uniqueId)
    }

    fun removeUser(user: U) {
        removeUser(user.uniqueId)
    }

    fun getUser(uniqueId: UUID): U? {
        return database.getUser(uniqueId, useCache, type)
    }

    companion object {
        private val defaultInstance = SayanVanishAPI<User>()

        @JvmStatic
        fun getInstance(): SayanVanishAPI<User> {
            return defaultInstance
        }

        fun UUID.user(): User? {
            return getInstance().getUser(this)
        }
    }

}