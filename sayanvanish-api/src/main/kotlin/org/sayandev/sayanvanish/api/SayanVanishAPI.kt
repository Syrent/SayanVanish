package org.sayandev.sayanvanish.api

import org.sayandev.sayanvanish.api.database.DatabaseMethod
import org.sayandev.sayanvanish.api.database.databaseConfig
import org.sayandev.sayanvanish.api.database.redis.RedisDatabase
import org.sayandev.sayanvanish.api.database.sql.SQLDatabase
import java.util.*

open class SayanVanishAPI<U: VanishUser>(val type: Class<out VanishUser>) {
    constructor(): this(VanishUser::class.java)

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
        for (user in database.getVanishUsers().filter { user -> user.serverId == Platform.get().serverId }) {
            user.isOnline = false
            user.save()
        }
        database.purgeUsers(Platform.get().serverId)
    }

    fun getPlatform(): Platform {
        return Platform.get()
    }

    fun isVanished(uniqueId: UUID, useCache: Boolean = true): Boolean {
        return database.getVanishUser(uniqueId, useCache)?.isVanished == true
    }

    fun isVanished(uniqueId: UUID): Boolean {
        return database.getVanishUser(uniqueId, true)?.isVanished == true
    }

    fun canSee(user: U?, target: U): Boolean {
        if (!target.isVanished) return true
        val vanishLevel = user?.vanishLevel ?: -1
        return vanishLevel >= target.vanishLevel
    }

    fun getUser(uniqueId: UUID, useCache: Boolean = true): U? {
        return database.getVanishUser(uniqueId, useCache)
    }

    fun getUser(uniqueId: UUID): U? {
        return getUser(uniqueId, true)
    }

    fun getOnlineUsers(): List<U> {
        return database.getVanishUsers().filter { it.isOnline }
    }

    fun getVanishedUsers(): List<U> {
        return database.getVanishUsers().filter { it.isVanished }
    }

    private fun logDatabaseError() {
        Platform.get().logger.severe("Database connection failed. Disabling the plugin.")
        Platform.get().logger.severe("Please check the following:")
        Platform.get().logger.severe("- Make sure your database server is not misconfigured.")
        Platform.get().logger.severe("- Make sure your database server is running.")
        Platform.get().logger.severe("Here's the full error trace:")
    }

    companion object {
        private val defaultInstance = SayanVanishAPI<VanishUser>()

        @JvmStatic
        fun getInstance(): SayanVanishAPI<VanishUser> {
            return defaultInstance
        }

        @JvmStatic
        fun UUID.user(): VanishUser? {
            return getInstance().getUser(this)
        }

        /*fun UUID.asyncUser(result: (User?) -> Unit) {
            getInstance().getUserAsync(this, result)
        }*/
    }

}