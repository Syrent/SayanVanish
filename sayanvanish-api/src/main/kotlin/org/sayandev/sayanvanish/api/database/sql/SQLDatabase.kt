package org.sayandev.sayanvanish.api.database.sql

import org.sayandev.sayanvanish.api.BasicUser
import org.sayandev.sayanvanish.api.Platform
import org.sayandev.sayanvanish.api.User
import org.sayandev.sayanvanish.api.User.Companion.convert
import org.sayandev.sayanvanish.api.VanishOptions
import org.sayandev.sayanvanish.api.database.Database
import org.sayandev.sayanvanish.api.database.DatabaseMethod
import org.sayandev.stickynote.core.database.Query
import org.sayandev.stickynote.core.database.mysql.MySQLCredentials
import org.sayandev.stickynote.core.database.mysql.MySQLDatabase
import org.sayandev.stickynote.core.database.sqlite.SQLiteDatabase
import java.io.File
import java.util.*
import kotlin.reflect.safeCast

class SQLDatabase<U : User>(
    val config: SQLConfig,
    val type: Class<out User>,
    override var useCache: Boolean = true
) : Database<U> {

    override var cache = mutableMapOf<UUID, U>()
    var basicCache = mutableMapOf<UUID, BasicUser>()
    val database: org.sayandev.stickynote.core.database.Database = when (config.method) {
        SQLConfig.SQLMethod.MYSQL -> {
            MySQLDatabase(
                MySQLCredentials.Companion.mySQLCredentials(config.host, config.port, config.database, config.poolProperties.useSSL, config.username, config.password),
                config.poolProperties.maximumPoolSize,
                config.poolProperties.verifyServerCertificate,
                let {
                    try {
                        Class.forName("com.mysql.cj.jdbc.Driver")
                        "com.mysql.cj.jdbc.Driver"
                    } catch (e: ClassNotFoundException) {
                        Class.forName("com.mysql.jdbc.Driver")
                        "com.mysql.jdbc.Driver"
                    }
                },
                config.poolProperties.keepaliveTime,
                config.poolProperties.connectionTimeout,
                config.poolProperties.minimumIdle,
                config.poolProperties.maximumLifetime,
                config.poolProperties.allowPublicKeyRetrieval
            )
        }
        SQLConfig.SQLMethod.SQLITE -> {
            SQLiteDatabase(File(Platform.get().rootDirectory, "storage.db"), Platform.get().logger)
        }
        else -> {
            throw NullPointerException("Database method with id `${config.method.name}` doesn't exist, available database types: ${DatabaseMethod.entries.map { it.name.lowercase() }}")
        }
    }

    override fun initialize() {
        database.runQuery(Query.query("CREATE TABLE IF NOT EXISTS ${config.tablePrefix}users (UUID VARCHAR(64),username VARCHAR(16),server VARCHAR(128),is_vanished INT,is_online INT,vanish_level INT,PRIMARY KEY (UUID));")).result?.close()
        database.runQuery(Query.query("CREATE TABLE IF NOT EXISTS ${config.tablePrefix}basic_users (UUID VARCHAR(64),username VARCHAR(16),server VARCHAR(128),PRIMARY KEY (UUID));")).result?.close()
        database.runQuery(Query.query("CREATE TABLE IF NOT EXISTS ${config.tablePrefix}queue (UUID VARCHAR(64), vanished VARCHAR(16),PRIMARY KEY (UUID));")).result?.close()
    }

    override fun connect() {
        cache.clear()
        database.connect()
    }

    override fun disconnect() {
        database.shutdown()
        cache.clear()
    }

    override fun getUser(uniqueId: UUID): U? {
        val cacheUser = cache[uniqueId]
        if (useCache) {
            if (cacheUser == null) {
                return null
            }
            return (type.kotlin.safeCast(cacheUser) as? U) ?: (cacheUser.convert(type) as U)
        }

        val result = database.runQuery(Query.query("SELECT * FROM ${config.tablePrefix}users WHERE UUID = ?;").setStatementValue(1, uniqueId.toString())).result ?: return null
        if (!result.next()) return null
        val user = object : User {
            override val uniqueId: UUID = UUID.fromString(result.getString("UUID"))
            override var username: String = result.getString("username")
            override var serverId: String = result.getString("server")
            override var currentOptions: VanishOptions = VanishOptions.defaultOptions()

            override var isVanished: Boolean = result.getBoolean("is_vanished")
            override var isOnline: Boolean = result.getBoolean("is_online")
            override var vanishLevel: Int = result.getInt("vanish_level")
        }
        val typedUser = (type.kotlin.safeCast(user) as? U) ?: (user.convert(type) as U)
        cache[uniqueId] = typedUser
        result.close()
        return typedUser
    }

    override fun getUsersAsync(result: (List<U>) -> Unit) {
        database.queueQuery(Query.query("SELECT * FROM ${config.tablePrefix}users;")).completableFuture.whenComplete { resultSet, error ->
            error?.printStackTrace()

            val users = mutableListOf<U>()
            while (resultSet.next()) {
                val user = object : User {
                    override val uniqueId: UUID = UUID.fromString(resultSet.getString("UUID"))
                    override var username: String = resultSet.getString("username")
                    override var serverId: String = resultSet.getString("server")
                    override var currentOptions: VanishOptions = VanishOptions.defaultOptions()

                    override var isVanished: Boolean = resultSet.getBoolean("is_vanished")
                    override var isOnline: Boolean = resultSet.getBoolean("is_online")
                    override var vanishLevel: Int = resultSet.getInt("vanish_level")
                }
                users.add((type.kotlin.safeCast(user) as? U) ?: (user.convert(type) as U))
            }
            resultSet.close()

            result(users)
        }
    }

    override fun getUsers(): List<U> {
        if (useCache) {
            return cache.values.toList()
        }

        val result = database.runQuery(Query.query("SELECT * FROM ${config.tablePrefix}users;")).result ?: return emptyList()
        val users = mutableListOf<U>()
        while (result.next()) {
            val user = object : User {
                override val uniqueId: UUID = UUID.fromString(result.getString("UUID"))
                override var username: String = result.getString("username")
                override var serverId: String = result.getString("server")
                override var currentOptions: VanishOptions = VanishOptions.defaultOptions()

                override var isVanished: Boolean = result.getBoolean("is_vanished")
                override var isOnline: Boolean = result.getBoolean("is_online")
                override var vanishLevel: Int = result.getInt("vanish_level")
            }
            users.add((type.kotlin.safeCast(user) as? U) ?: (user.convert(type) as U))
        }
        result.close()

        return users
    }

    override fun getBasicUsers(useCache: Boolean): List<BasicUser> {
        if (useCache) {
            return basicCache.values.toList()
        }

        val result = database.runQuery(Query.query("SELECT * FROM ${config.tablePrefix}basic_users;")).result ?: return emptyList()
        val users = mutableListOf<BasicUser>()
        while (result.next()) {
            val user = BasicUser.create(
                UUID.fromString(result.getString("UUID")),
                result.getString("username"),
                result.getString("server")
            )
            users.add(user)
        }
        result.close()

        return users
    }

    override fun getBasicUsersAsync(result: (List<BasicUser>) -> Unit) {
        database.queueQuery(Query.query("SELECT * FROM ${config.tablePrefix}basic_users;")).completableFuture.whenComplete { resultSet, error ->
            error?.printStackTrace()

            val users = mutableListOf<BasicUser>()
            while (resultSet.next()) {
                val user = BasicUser.create(
                    UUID.fromString(resultSet.getString("UUID")),
                    resultSet.getString("username"),
                    resultSet.getString("server")
                )
                users.add(user)
            }
            resultSet.close()

            result(users)
        }
    }

    override fun addUser(user: U) {
        cache[user.uniqueId] = user
        if (!hasUser(user.uniqueId)) {
            database.runQuery(
                Query.query("INSERT ${if (config.method == SQLConfig.SQLMethod.MYSQL) "IGNORE " else ""}INTO ${config.tablePrefix}users (UUID, username, server, is_vanished, is_online, vanish_level) VALUES (?,?,?,?,?,?);")
                    .setStatementValue(1, user.uniqueId.toString())
                    .setStatementValue(2, user.username)
                    .setStatementValue(3, user.serverId)
                    .setStatementValue(4, user.isVanished)
                    .setStatementValue(5, user.isOnline)
                    .setStatementValue(6, user.vanishLevel)
            ).result?.close()
        } else {
            updateUser(user)
        }
    }

    override fun addBasicUser(user: BasicUser) {
        basicCache[user.uniqueId] = user
        if (!hasBasicUser(user.uniqueId, false)) {
            database.runQuery(
                Query.query("INSERT ${if (config.method == SQLConfig.SQLMethod.MYSQL) "IGNORE " else ""}INTO ${config.tablePrefix}basic_users (UUID, username, server) VALUES (?,?,?);")
                    .setStatementValue(1, user.uniqueId.toString())
                    .setStatementValue(2, user.username)
                    .setStatementValue(3, user.serverId)
            ).result?.close()
        } else {
            if (user.serverId != Platform.get().serverId) {
                updateBasicUser(user)
            }
        }
    }

    override fun hasUser(uniqueId: UUID): Boolean {
        val queryResult = database.runQuery(Query.query("SELECT * FROM ${config.tablePrefix}users WHERE UUID = ?;").setStatementValue(1, uniqueId.toString()))
        val result = queryResult.result ?: return false
        val hasNext = result.next()
        result.close()
        return hasNext
    }

    override fun hasBasicUser(uniqueId: UUID, useCache: Boolean): Boolean {
        if (useCache) {
            return basicCache.contains(uniqueId)
        }
        val queryResult = database.runQuery(Query.query("SELECT * FROM ${config.tablePrefix}basic_users WHERE UUID = ?;").setStatementValue(1, uniqueId.toString()))
        val result = queryResult.result ?: return false
        val hasNext = result.next()
        result.close()
        return hasNext
    }

    override fun removeUser(uniqueId: UUID) {
        cache.remove(uniqueId)
        database.runQuery(Query.query("DELETE FROM ${config.tablePrefix}users WHERE UUID = ?;").setStatementValue(1, uniqueId.toString())).result?.close()
    }

    override fun removeBasicUser(uniqueId: UUID) {
        basicCache.remove(uniqueId)
        database.runQuery(Query.query("DELETE FROM ${config.tablePrefix}basic_users WHERE UUID = ?;").setStatementValue(1, uniqueId.toString())).result?.close()
    }

    override fun updateUser(user: U) {
        cache[user.uniqueId] = user
        database.runQuery(
            Query.query("UPDATE ${config.tablePrefix}users SET username = ?, is_vanished = ?, is_online = ?, vanish_level = ? WHERE UUID = ?;")
                .setStatementValue(1, user.username)
                .setStatementValue(2, user.isVanished)
                .setStatementValue(3, user.isOnline)
                .setStatementValue(4, user.vanishLevel)
                .setStatementValue(5, user.uniqueId.toString())
        ).result?.close()
    }

    override fun updateBasicUser(user: BasicUser) {
        basicCache[user.uniqueId] = user
        database.runQuery(
            Query.query("UPDATE ${config.tablePrefix}basic_users SET username = ?, server = ? WHERE UUID = ?;")
                .setStatementValue(1, user.username)
                .setStatementValue(2, user.serverId)
                .setStatementValue(3, user.uniqueId.toString())
        ).result?.close()
    }

    override fun isInQueue(uniqueId: UUID, inQueue: (Boolean) -> Unit) {
        database.queueQuery(Query.query("SELECT * FROM ${config.tablePrefix}queue WHERE UUID = ?;").setStatementValue(1, uniqueId.toString())).completableFuture.whenComplete { result, error ->
            error?.printStackTrace()

            val hasNext = result.next()
            inQueue(hasNext)
        }
    }

    override fun addToQueue(uniqueId: UUID, vanished: Boolean) {
        isInQueue(uniqueId) { inQueue ->
            if (!inQueue) {
                database.queueQuery(Query.query("INSERT INTO ${config.tablePrefix}queue (UUID, vanished) VALUES (?,?);").setStatementValue(1, uniqueId.toString()).setStatementValue(2, vanished.toString()))
            } else {
                database.queueQuery(Query.query("UPDATE ${config.tablePrefix}queue SET vanished = ? WHERE UUID = ?;").setStatementValue(1, vanished.toString()).setStatementValue(2, uniqueId.toString()))
            }
        }
    }

    override fun getFromQueue(uniqueId: UUID, result: (Boolean) -> Unit) {
        database.queueQuery(Query.query("SELECT * FROM ${config.tablePrefix}queue WHERE UUID = ?;").setStatementValue(1, uniqueId.toString())).completableFuture.whenComplete { resultSet, error ->
            error?.printStackTrace()

            if (!resultSet.next()) result(false)
            result(resultSet.getString("vanished").toBoolean())
        }
    }

    override fun removeFromQueue(uniqueId: UUID) {
        database.queueQuery(Query.query("DELETE FROM ${config.tablePrefix}queue WHERE UUID = ?;").setStatementValue(1, uniqueId.toString()))
    }

    override fun purgeCache() {
        cache.clear()
        basicCache.clear()
    }

    override fun purge() {
        database.runQuery(Query.query("DELETE FROM ${config.tablePrefix}users;")).result?.close()
        database.runQuery(Query.query("DELETE FROM ${config.tablePrefix}basic_users;")).result?.close()
        database.runQuery(Query.query("DELETE FROM ${config.tablePrefix}queue;")).result?.close()
    }

    override fun purgeBasic() {
        database.runQuery(Query.query("DELETE FROM ${config.tablePrefix}basic_users;")).result?.close()
        database.runQuery(Query.query("DELETE FROM ${config.tablePrefix}queue;")).result?.close()
    }

    override fun purgeBasic(serverId: String) {
        database.runQuery(Query.query("DELETE FROM ${config.tablePrefix}basic_users WHERE server = ?;").setStatementValue(1, serverId)).result?.close()
    }

}
