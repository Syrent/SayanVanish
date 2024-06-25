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

    fun isVanished(uniqueId: UUID): Boolean {
        return database.getUser(uniqueId, type)?.isVanished == true
    }

    fun canSee(user: U, target: U): Boolean {
        return user.vanishLevel >= target.vanishLevel
    }

    companion object {
        private val defaultInstance = SayanVanishAPI<User>()

        @JvmStatic
        fun getInstance(): SayanVanishAPI<User> {
            return defaultInstance
        }

        fun UUID.user(): User? {
            return getInstance().database.getUser(this)
        }

        fun UUID.asyncUser(result: (User?) -> Unit) {
            getInstance().database.getUserAsync(this, result)
        }
    }

}