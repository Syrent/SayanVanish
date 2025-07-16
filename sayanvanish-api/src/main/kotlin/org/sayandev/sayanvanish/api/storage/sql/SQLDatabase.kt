package org.sayandev.sayanvanish.api.storage.sql

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import org.jetbrains.exposed.v1.jdbc.transactions.experimental.suspendedTransactionAsync
import org.sayandev.sayanvanish.api.Platform
import org.sayandev.sayanvanish.api.User
import org.sayandev.sayanvanish.api.VanishUser
import org.sayandev.sayanvanish.api.storage.Database
import org.sayandev.sayanvanish.api.storage.StorageConfig
import org.sayandev.stickynote.core.coroutine.dispatcher.AsyncDispatcher
import org.sayandev.stickynote.core.utils.CoroutineUtils
import org.sayandev.stickynote.core.utils.launch
import java.util.*

class SQLDatabase(
    val config: StorageConfig,
) : Database {

    override var connected = false

    override val dispatcher =
        AsyncDispatcher(
            "${Platform.get().pluginName.lowercase()}-${config.sql.method}-thread",
            config.sql.threadCount,
        )

    val tables = listOf(
        User.Schema,
        VanishUser.Schema,
    )

    lateinit var database: org.jetbrains.exposed.v1.jdbc.Database

    override suspend fun initialize(): Deferred<Boolean> {
        SchemaUtils.createMissingTablesAndColumns(
            *tables.toTypedArray(),
            withLogs = false,
        )
        return CompletableDeferred(true)
    }

    override suspend fun connect(): Deferred<Boolean> {
        database =
            org.jetbrains.exposed.v1.jdbc.Database.connect(
                url =
                    when (config.sql.method) {
                        SQLConfig.SQLMethod.SQLITE -> "jdbc:sqlite:${Platform.get().rootDirectory.absolutePath}/database"
                        SQLConfig.SQLMethod.MARIADB -> "jdbc:mariadb://${config.sql.host}:${config.sql.port}/${config.sql.database}?autoReconnect=true"
                        SQLConfig.SQLMethod.MYSQL -> "jdbc:mysql://${config.sql.host}:${config.sql.port}/${config.sql.database}?autoReconnect=true"
                    },
                driver =
                    when (config.sql.method) {
                        SQLConfig.SQLMethod.SQLITE -> "org.sqlite.JDBC"
                        SQLConfig.SQLMethod.MARIADB -> "org.mariadb.jdbc.Driver"
                        SQLConfig.SQLMethod.MYSQL -> "com.mysql.cj.jdbc.Driver"
                    },
                user = config.sql.username,
                password = config.sql.password,
            )
        TransactionManager.defaultDatabase = database

        return CompletableDeferred(true)
    }

    override suspend fun disconnect(): Deferred<Boolean> {
        TransactionManager.closeAndUnregister(database)
        return CompletableDeferred(true)
    }

    override suspend fun getVanishUser(uniqueId: UUID): Deferred<VanishUser?> {
        return async {
            (VanishUser.Schema innerJoin User.Schema)
                .selectAll()
                .firstOrNull { it[VanishUser.Schema.uniqueId] == uniqueId }
                ?.let { result ->
                    VanishUser.of(
                        result[VanishUser.Schema.uniqueId],
                        result[User.Schema.username],
                        result[User.Schema.serverId],
                        result[VanishUser.Schema.isVanished],
                        result[User.Schema.isOnline],
                        result[VanishUser.Schema.vanishLevel]
                    )
                }
        }
    }

    override suspend fun getVanishUsers(): Deferred<List<VanishUser>> {
        return async {
            (VanishUser.Schema innerJoin User.Schema)
                .selectAll()
                .map { result ->
                    VanishUser.of(
                        result[VanishUser.Schema.uniqueId],
                        result[User.Schema.username],
                        result[User.Schema.serverId],
                        result[VanishUser.Schema.isVanished],
                        result[User.Schema.isOnline],
                        result[VanishUser.Schema.vanishLevel]
                    )
                }
        }
    }

    override suspend fun getUser(uniqueId: UUID): Deferred<User?> {
        return async {
            User.Schema
                .selectAll()
                .firstOrNull { it[User.Schema.uniqueId] == uniqueId }
                ?.let { result ->
                    User.of(
                        result[User.Schema.uniqueId],
                        result[User.Schema.username],
                        result[User.Schema.isOnline],
                        result[User.Schema.serverId]
                    )
                }
        }
    }

    override suspend fun getUsers(): Deferred<List<User>> {
        return async {
            User.Schema
                .selectAll()
                .map { result ->
                    User.of(
                        result[User.Schema.uniqueId],
                        result[User.Schema.username],
                        result[User.Schema.isOnline],
                        result[User.Schema.serverId]
                    )
                }
        }
    }

    override suspend fun addVanishUser(vanishUser: VanishUser): Deferred<Boolean> {
        return async {
            VanishUser.Schema.upsert { row ->
                row[uniqueId] = vanishUser.uniqueId
                row[isVanished] = vanishUser.isVanished
                row[vanishLevel] = vanishUser.vanishLevel
            }.isIgnore
        }
    }

    override suspend fun saveUser(user: User): Deferred<Boolean> {
        return async {
            User.Schema.upsert { row ->
                row[uniqueId] = user.uniqueId
                row[username] = user.username
                row[serverId] = user.serverId
                row[isOnline] = user.isOnline
            }.isIgnore
        }
    }

    override suspend fun hasVanishUser(uniqueId: UUID): Deferred<Boolean> {
        return async {
            VanishUser.Schema
                .selectAll()
                .any { it[VanishUser.Schema.uniqueId] == uniqueId }
        }
    }

    override suspend fun hasUser(uniqueId: UUID): Deferred<Boolean> {
        return async {
            User.Schema
                .selectAll()
                .any { it[User.Schema.uniqueId] == uniqueId }
        }
    }

    override suspend fun removeVanishUser(uniqueId: UUID): Deferred<Boolean> {
        return async {
            VanishUser.Schema
                .deleteWhere { VanishUser.Schema.uniqueId eq uniqueId }
            true
        }
    }

    override suspend fun removeUser(uniqueId: UUID): Deferred<Boolean> {
        return async {
            User.Schema
                .deleteWhere { User.Schema.uniqueId eq uniqueId }
            true
        }
    }

    override suspend fun updateVanishUser(user: VanishUser): Deferred<Boolean> {
        return async {
            VanishUser.Schema.upsert { row ->
                row[uniqueId] = user.uniqueId
                row[isVanished] = user.isVanished
                row[vanishLevel] = user.vanishLevel
                row[currentOptions] = user.currentOptions.toJson()
            }.isIgnore
        }
    }

    override suspend fun updateUser(user: User): Deferred<Boolean> {
        return async {
            User.Schema.upsert { row ->
                row[uniqueId] = user.uniqueId
                row[username] = user.username
                row[serverId] = user.serverId
                row[isOnline] = user.isOnline
            }.isIgnore
        }
    }

    override suspend fun purgeAllTables(): Deferred<Boolean> {
        tables.map { table -> async { table.deleteAll() } }.awaitAll()
        return CompletableDeferred(true)
    }

    override suspend fun purgeUsers(): Deferred<Boolean> {
        return async {
            User.Schema.deleteAll()
            true
        }
    }

    override suspend fun purgeUsers(serverId: String): Deferred<Boolean> {
        return async {
            User.Schema.deleteWhere { User.Schema.serverId eq serverId }
            true
        }
    }

    fun <T> async(statement: suspend Transaction.() -> T): Deferred<T> {
        return CoroutineUtils.async(dispatcher) {
            suspendedTransactionAsync(
                dispatcher,
                database,
                statement = statement,
            ).await()
        }
    }

}
