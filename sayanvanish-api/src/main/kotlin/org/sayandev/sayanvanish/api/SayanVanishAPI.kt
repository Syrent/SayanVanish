package org.sayandev.sayanvanish.api

import org.sayandev.sayanvanish.api.database.DatabaseMethod
import org.sayandev.sayanvanish.api.database.databaseConfig
import org.sayandev.sayanvanish.api.database.redis.RedisDatabase
import org.sayandev.sayanvanish.api.database.sql.SQLDatabase
import java.util.*

open class SayanVanishAPI<U: User>(val type: Class<out User>) {
    constructor(): this(User::class.java)

    var databaseConnected: Boolean = true

    val database = when (databaseConfig.method) {
        DatabaseMethod.SQL -> {
            try {
                SQLDatabase<U>(databaseConfig.sql, type, databaseConfig.useCacheWhenAvailable).apply {
                    this.connect()
                    this.initialize()
                }
            } catch (e: Exception) {
                databaseConnected = false
                logDatabaseError()
                throw e
            }
        }
        DatabaseMethod.REDIS -> {
            try {
                RedisDatabase<U>(databaseConfig.redis, type, databaseConfig.useCacheWhenAvailable).apply {
                    this.initialize()
                    this.connect()
                }
            } catch (e: Exception) {
                databaseConnected = false
                logDatabaseError()
                throw e
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

    fun isVanished(uniqueId: UUID): Boolean {
        return database.getUser(uniqueId, true)?.isVanished == true
    }

    fun canSee(user: U?, target: U): Boolean {
        if (!target.isVanished) return true
        val vanishLevel = user?.vanishLevel ?: -1
        return vanishLevel >= target.vanishLevel
    }

    fun getUser(uniqueId: UUID, useCache: Boolean = true): U? {
        return database.getUser(uniqueId, useCache)
    }

    fun getUser(uniqueId: UUID): U? {
        return getUser(uniqueId, true)
    }

    fun getOnlineUsers(): List<U> {
        return database.getUsers().filter { it.isOnline }
    }

    fun getVanishedUsers(): List<U> {
        return database.getUsers().filter { it.isVanished }
    }

    private fun logDatabaseError() {
        Platform.get().logger.severe("Database connection failed. Disabling the plugin.")
        Platform.get().logger.severe("Please check the following:")
        Platform.get().logger.severe("- Make sure you have `\"` before and after database ip address.")
        Platform.get().logger.severe("- Make sure your database server is not misconfigured.")
        Platform.get().logger.severe("- Make sure your database server is running.")
        Platform.get().logger.severe("Here's the full error trace:")
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