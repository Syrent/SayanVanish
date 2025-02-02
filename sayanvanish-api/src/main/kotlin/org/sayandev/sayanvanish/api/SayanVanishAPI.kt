package org.sayandev.sayanvanish.api

import org.sayandev.sayanvanish.api.database.DatabaseMethod
import org.sayandev.sayanvanish.api.database.databaseConfig
import org.sayandev.sayanvanish.api.database.redis.RedisDatabase
import org.sayandev.sayanvanish.api.database.sql.SQLDatabase
import java.util.*

open class SayanVanishAPI<U: User>(val type: Class<out User>) {
    constructor(): this(User::class.java)

    val database = when (databaseConfig.method) {
        DatabaseMethod.SQL -> {
            SQLDatabase<U>(databaseConfig.sql, type, databaseConfig.useCacheWhenAvailable).apply {
                this.connect()
                this.initialize()
            }
        }
        DatabaseMethod.REDIS -> {
            RedisDatabase<U>(databaseConfig.redis, type, databaseConfig.useCacheWhenAvailable).apply {
                this.initialize()
                this.connect()
            }
        }
    }

    init {
        for (user in database.getUsers().filter { user -> user.serverId == Platform.get().serverId }) {
            user.isOnline = false
            user.save()
        }
        database.purgeBasic(Platform.get().serverId)
    }

    fun getPlatform(): Platform {
        return Platform.get()
    }

    fun isVanished(uniqueId: UUID, useCache: Boolean = true): Boolean {
        return database.getUser(uniqueId, useCache)?.isVanished == true
    }

    fun canSee(user: U?, target: U): Boolean {
        if (!target.isVanished) return true
        val vanishLevel = user?.vanishLevel ?: -1
        return vanishLevel >= target.vanishLevel
    }

    fun getUser(uniqueId: UUID, useCache: Boolean = true): U? {
        return database.getUser(uniqueId, useCache)
    }

    fun getOnlineUsers(): List<U> {
        return database.getUsers().filter { it.isOnline }
    }

    fun getVanishedUsers(): List<U> {
        return database.getUsers().filter { it.isVanished }
    }

    companion object {
        private val defaultInstance = SayanVanishAPI<User>()

        @JvmStatic
        fun getInstance(): SayanVanishAPI<User> {
            return defaultInstance
        }

        @JvmStatic
        fun UUID.user(): User? {
            return getInstance().getUser(this)
        }

        /*fun UUID.asyncUser(result: (User?) -> Unit) {
            getInstance().getUserAsync(this, result)
        }*/
    }

}