package org.sayandev.sayanvanish.api.database.sql

import org.sayandev.sayanvanish.api.BasicUser
import org.sayandev.sayanvanish.api.Platform
import org.sayandev.sayanvanish.api.User
import org.sayandev.sayanvanish.api.User.Companion.cast
import org.sayandev.sayanvanish.api.VanishOptions
import org.sayandev.sayanvanish.api.database.Database
import org.sayandev.sayanvanish.api.database.DatabaseMethod
import org.sayandev.stickynote.core.database.Query
import org.sayandev.stickynote.core.database.mysql.MySQLCredentials
import org.sayandev.stickynote.core.database.mysql.MySQLDatabase
import org.sayandev.stickynote.core.database.sqlite.SQLiteDatabase
import sun.awt.www.content.audio.wav
import java.io.File
import java.util.*
import java.util.function.Consumer
import kotlin.reflect.KClass
import kotlin.reflect.safeCast


class SQLDatabase<U : User>(
    val config: SQLConfig
) : Database<U> {

    val cache = mutableMapOf<UUID, U>()
    val basicCache = mutableMapOf<UUID, BasicUser>()
    val driverClass = try {
        Class.forName("com.mysql.cj.jdbc.Driver")
        "com.mysql.cj.jdbc.Driver"
    } catch (e: ClassNotFoundException) {
        Class.forName("com.mysql.jdbc.Driver")
        "com.mysql.jdbc.Driver"
    }
    val database: org.sayandev.stickynote.core.database.Database = when (config.method) {
        SQLConfig.SQLMethod.MYSQL -> {
            MySQLDatabase(MySQLCredentials.Companion.mySQLCredentials(config.host, config.port, config.database, config.poolProperties.useSSL, config.username, config.password), config.poolProperties.maximumPoolSize, false, driverClass, config.poolProperties.keepaliveTime, config.poolProperties.connectionTimeout)
        }
        SQLConfig.SQLMethod.SQLITE -> {
            SQLiteDatabase(File(Platform.get().rootDirectory, "storage.db"), Platform.get().logger)
        }
        else -> {
            throw NullPointerException("Database method with id `${config.method.name}` doesn't exist, available database types: ${DatabaseMethod.entries.map { it.name.lowercase() }}")
        }
    }

    override fun initialize() {
        database.runQuery(Query.query("CREATE TABLE IF NOT EXISTS ${config.tablePrefix}users (UUID VARCHAR(64),username VARCHAR(16),server VARCHAR(128),is_vanished INT,is_online INT,vanish_level INT,PRIMARY KEY (UUID));"))
        database.runQuery(Query.query("CREATE TABLE IF NOT EXISTS ${config.tablePrefix}basic_users (UUID VARCHAR(64),username VARCHAR(16),server VARCHAR(128),PRIMARY KEY (UUID));"))
        database.runQuery(Query.query("CREATE TABLE IF NOT EXISTS ${config.tablePrefix}queue (UUID VARCHAR(64), vanished VARCHAR(16),PRIMARY KEY (UUID));"))
    }

    override fun connect() {
        cache.clear()
        database.connect()
    }

    override fun disconnect() {
        database.shutdown()
        cache.clear()
    }

    override fun getUser(uniqueId: UUID, useCache: Boolean, type: KClass<out User>): U? {
        if (useCache && cache.contains(uniqueId)) {
            return cache[uniqueId]
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
        val typedUser = (type.safeCast(user) as? U) ?: (user.cast(type) as U)
        cache[uniqueId] = typedUser
        return typedUser
    }

    override fun getUsers(useCache: Boolean): List<U> {
        return getUsers(useCache, User::class)
    }

    override fun getUsers(useCache: Boolean, type: KClass<out User>): List<U> {
        /*if (useCache) {
            return cache.values.toList()
        }*/

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
            users.add((type.safeCast(user) as? U) ?: (user.cast(type) as U))
        }

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

        return users
    }

    override fun getUser(uniqueId: UUID, useCache: Boolean): U? {
        return getUser(uniqueId, useCache, User::class)
    }

    override fun addUser(user: U) {
        cache[user.uniqueId] = user
        if (!hasUser(user.uniqueId, false)) {
            database.runQuery(
                Query.query("INSERT ${if (config.method == SQLConfig.SQLMethod.MYSQL) "IGNORE " else ""}INTO ${config.tablePrefix}users (UUID, username, server, is_vanished, is_online, vanish_level) VALUES (?,?,?,?,?,?);")
                    .setStatementValue(1, user.uniqueId.toString())
                    .setStatementValue(2, user.username)
                    .setStatementValue(3, user.serverId)
                    .setStatementValue(4, user.isVanished)
                    .setStatementValue(5, user.isOnline)
                    .setStatementValue(6, user.vanishLevel)
            )
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
            )
        } else {
            if (user.serverId != Platform.get().id) {
                updateBasicUser(user)
            }
        }
    }

    override fun hasUser(uniqueId: UUID, useCache: Boolean): Boolean {
        if (useCache) {
            return cache.contains(uniqueId)
        }
        val queryResult = database.runQuery(Query.query("SELECT * FROM ${config.tablePrefix}users WHERE UUID = ?;").setStatementValue(1, uniqueId.toString()))
        val result = queryResult.result ?: return false
        val hasNext = result.next()
        return hasNext
    }

    override fun hasBasicUser(uniqueId: UUID, useCache: Boolean): Boolean {
        if (useCache) {
            return basicCache.contains(uniqueId)
        }
        val queryResult = database.runQuery(Query.query("SELECT * FROM ${config.tablePrefix}basic_users WHERE UUID = ?;").setStatementValue(1, uniqueId.toString()))
        val result = queryResult.result ?: return false
        val hasNext = result.next()
        return hasNext
    }

    override fun removeUser(uniqueId: UUID) {
        cache.remove(uniqueId)
        database.runQuery(Query.query("DELETE FROM ${config.tablePrefix}users WHERE UUID = ?;").setStatementValue(1, uniqueId.toString()))
    }

    override fun removeBasicUser(uniqueId: UUID) {
        basicCache.remove(uniqueId)
        database.runQuery(Query.query("DELETE FROM ${config.tablePrefix}basic_users WHERE UUID = ?;").setStatementValue(1, uniqueId.toString()))
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
        )
    }

    override fun updateBasicUser(user: BasicUser) {
        basicCache[user.uniqueId] = user
        database.runQuery(
            Query.query("UPDATE ${config.tablePrefix}basic_users SET username = ?, server = ? WHERE UUID = ?;")
                .setStatementValue(1, user.username)
                .setStatementValue(2, user.serverId)
                .setStatementValue(3, user.uniqueId.toString())
        )
    }

    override fun isInQueue(uniqueId: UUID, inQueue: Consumer<Boolean>) {
        database.queueQuery(Query.query("SELECT * FROM ${config.tablePrefix}queue WHERE UUID = ?;").setStatementValue(1, uniqueId.toString())).completableFuture.whenComplete { result, error ->
            error?.printStackTrace()

            val hasNext = result.next()
            inQueue.accept(hasNext)
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

    override fun getFromQueue(uniqueId: UUID, result: Consumer<Boolean>) {
        database.queueQuery(Query.query("SELECT * FROM ${config.tablePrefix}queue WHERE UUID = ?;").setStatementValue(1, uniqueId.toString())).completableFuture.whenComplete { resultSet, error ->
            error?.printStackTrace()

            if (!resultSet.next()) result.accept(false)
            result.accept(resultSet.getString("vanished").toBoolean())
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
        database.runQuery(Query.query("DELETE FROM ${config.tablePrefix}users;"))
        database.runQuery(Query.query("DELETE FROM ${config.tablePrefix}basic_users;"))
        database.runQuery(Query.query("DELETE FROM ${config.tablePrefix}queue;"))
    }

    override fun purgeBasic() {
        database.runQuery(Query.query("DELETE FROM ${config.tablePrefix}basic_users;"))
        database.runQuery(Query.query("DELETE FROM ${config.tablePrefix}queue;"))
    }

    override fun purgeBasic(serverId: String) {
        database.runQuery(Query.query("DELETE FROM ${config.tablePrefix}basic_users WHERE server = ?;").setStatementValue(1, serverId))
    }

    override fun updateBasicCache() {
        val tempCache = mutableMapOf<UUID, BasicUser>()
        val result = database.runQuery(Query.query("SELECT * FROM ${config.tablePrefix}basic_users;")).result ?: return
        while (result.next()) {
            val user = BasicUser.create(
                UUID.fromString(result.getString("UUID")),
                result.getString("username"),
                result.getString("server")
            )
            tempCache[user.uniqueId] = user
        }
        basicCache.clear()
        basicCache.putAll(tempCache)
    }

}
