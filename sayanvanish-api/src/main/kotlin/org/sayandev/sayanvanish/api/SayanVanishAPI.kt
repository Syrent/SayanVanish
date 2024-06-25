package org.sayandev.sayanvanish.api

import org.sayandev.sayanvanish.api.database.DatabaseMethod
import org.sayandev.sayanvanish.api.database.databaseConfig
import org.sayandev.sayanvanish.api.database.redis.RedisDatabase
import org.sayandev.sayanvanish.api.database.sql.SQLDatabase
import java.util.*
import kotlin.reflect.KClass

open class SayanVanishAPI<U: User>(val type: KClass<out User>) {
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
        return database.getUsers(type)
    }

    fun getUsersAsync(result: (List<U>) -> Unit) {
        database.getUsersAsync(type, result)
    }

    fun getUsers(predicate: (U) -> Boolean): List<U> {
        return getUsers().filter(predicate)
    }

    fun getUsersAsync(predicate: (U) -> Boolean, result: (List<U>) -> Unit) {
        getUsersAsync {
            result(it.filter(predicate))
        }
    }

    fun getBasicUsers(useCache: Boolean = databaseConfig.useCacheWhenAvailable): List<BasicUser> {
        return database.getBasicUsers(useCache)
    }

    fun getBasicUsersAsync(result: (List<BasicUser>) -> Unit) {
        database.getBasicUsersAsync(result)
    }

    fun getSortedUsers(predicate: (U) -> Int) {
        getUsers().sortedByDescending(predicate)
    }

    fun getSortedUsersAsync(predicate: (U) -> Int, result: (List<U>) -> Unit) {
        getUsersAsync {
            result(it.sortedByDescending(predicate))
        }
    }

    fun isVanished(uniqueId: UUID): Boolean {
        return database.getUser(uniqueId, type)?.isVanished == true
    }

    fun getVanishedUsers(): Collection<U> {
        return getUsers().filter { it.isVanished }
    }

    fun getVanishedUsersAsync(result: (Collection<U>) -> Unit) {
        getUsersAsync {
            result(it.filter { user -> user.isVanished })
        }
    }

    fun getOnlineUsers(): Collection<U> {
        return getUsers(User::isOnline)
    }

    fun getOnlineUsersAsync(result: (Collection<U>) -> Unit) {
        getUsersAsync({ it.isOnline }, result)
    }

    fun canSee(user: U, target: U): Boolean {
        return user.vanishLevel >= target.vanishLevel
    }

    fun addUser(user: U) {
        database.addUser(user)
    }

    fun addUserAsync(user: U, result: () -> Unit) {
        database.addUserAsync(user, result)
    }

    fun addBasicUser(user: BasicUser) {
        database.addBasicUser(user)
    }

    fun removeUser(uniqueId: UUID) {
        database.removeUser(uniqueId)
    }

    fun removeUserAsync(uniqueId: UUID, result: () -> Unit) {
        database.removeUserAsync(uniqueId, result)
    }

    fun removeBasicUser(uniqueId: UUID) {
        database.removeBasicUser(uniqueId)
    }

    fun removeUser(user: U) {
        removeUser(user.uniqueId)
    }

    fun removeUserAsync(user: U, result: () -> Unit) {
        removeUserAsync(user.uniqueId, result)
    }

    fun getUser(uniqueId: UUID): U? {
        return database.getUser(uniqueId, type)
    }

    fun getUserAsync(uniqueId: UUID, result: (U?) -> Unit) {
        database.getUserAsync(uniqueId, type, result)
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

        fun UUID.asyncUser(result: (User?) -> Unit) {
            getInstance().getUserAsync(this, result)
        }
    }

}