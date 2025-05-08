package org.sayandev.sayanvanish.api.database.sql

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.awaitAll
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.experimental.suspendedTransactionAsync
import org.sayandev.sayanvanish.api.Platform
import org.sayandev.sayanvanish.api.PlatformAdapter
import org.sayandev.sayanvanish.api.Queue
import org.sayandev.sayanvanish.api.User
import org.sayandev.sayanvanish.api.VanishUser
import org.sayandev.sayanvanish.api.database.Database
import org.sayandev.sayanvanish.api.database.DatabaseConfig
import org.sayandev.stickynote.core.coroutine.dispatcher.AsyncDispatcher
import org.sayandev.stickynote.core.utils.CoroutineUtils
import org.sayandev.stickynote.core.utils.launch
import java.util.*
import java.util.concurrent.TimeUnit

class SQLDatabase(
    val config: DatabaseConfig,
) : Database {
    override val dispatcher =
        AsyncDispatcher(
            "${Platform.get().pluginName.lowercase()}-${config.sql.method}-thread",
            config.sqlDispatcherThreadCount,
        )

    val tables = listOf(
        User.Schema,
        VanishUser.Schema,
        Queue.Schema,
    )

    lateinit var database: org.jetbrains.exposed.sql.Database

    override suspend fun initialize(): Deferred<Boolean> {
        SchemaUtils.createMissingTablesAndColumns(
            *tables.toTypedArray(),
            withLogs = false,
        )
        return CompletableDeferred(true)
    }

    override suspend fun connect(): Deferred<Boolean> {
        val hikariConfig =
            HikariConfig().apply {
                jdbcUrl =
                    when (config.sql.method) {
                        SQLConfig.SQLMethod.SQLITE -> "jdbc:sqlite:${Platform.get().rootDirectory.absolutePath}/database"
                        SQLConfig.SQLMethod.MARIADB -> "jdbc:mariadb://${config.sql.host}:${config.sql.port}/${config.sql.database}?autoReconnect=true"
                        SQLConfig.SQLMethod.MYSQL -> "jdbc:mysql://${config.sql.host}:${config.sql.port}/${config.sql.database}?autoReconnect=true"
                    }
                driverClassName =
                    when (config.sql.method) {
                        SQLConfig.SQLMethod.SQLITE -> "org.sqlite.JDBC"
                        SQLConfig.SQLMethod.MARIADB -> "org.mariadb.jdbc.Driver"
                        SQLConfig.SQLMethod.MYSQL -> "com.mysql.cj.jdbc.Driver"
                    }
                username = config.sql.username
                password = config.sql.password
                maximumPoolSize = config.sql.poolProperties.maximumPoolSize
                this.minimumIdle = config.sql.poolProperties.minimumIdle
                this.keepaliveTime = config.sql.poolProperties.keepaliveTime
                if (config.sql.method != SQLConfig.SQLMethod.SQLITE) {
                    this.connectionTimeout = config.sql.poolProperties.connectionTimeout
                }
                this.maxLifetime = config.sql.poolProperties.maxLifetime

                this.addDataSourceProperty("socketTimeout", TimeUnit.SECONDS.toMillis(30).toString())
                this.addDataSourceProperty("cachePrepStmts", "true")
                this.addDataSourceProperty("prepStmtCacheSize", "250")
                this.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
                this.addDataSourceProperty("useServerPrepStmts", "true")
                this.addDataSourceProperty("useLocalSessionState", "true")
                this.addDataSourceProperty("rewriteBatchedStatements", "true")
                this.addDataSourceProperty("cacheResultSetMetadata", "true")
                this.addDataSourceProperty("cacheServerConfiguration", "true")
                this.addDataSourceProperty("elideSetAutoCommits", "true")
                this.addDataSourceProperty("maintainTimeStats", "false")
                this.addDataSourceProperty("alwaysSendSetIsolation", "false")
                this.addDataSourceProperty("cacheCallableStmts", "true")
                this.addDataSourceProperty("characterEncoding", "utf8")
                this.addDataSourceProperty("allowPublicKeyRetrieval", "true")
            }
        val dataSource = HikariDataSource(hikariConfig)
        database = org.jetbrains.exposed.sql.Database.connect(dataSource)
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

    override suspend fun addUser(user: User): Deferred<Boolean> {
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

    override suspend fun isInQueue(uniqueId: UUID): Deferred<Boolean> {
        return async {
            Queue.Schema
                .selectAll()
                .any { it[Queue.Schema.uniqueId] == uniqueId }
        }
    }

    override suspend fun addToQueue(uniqueId: UUID, vanished: Boolean): Deferred<Boolean> {
        return async {
            Queue.Schema.upsert { row ->
                row[Queue.Schema.uniqueId] = uniqueId
                row[Queue.Schema.vanished] = vanished
            }.isIgnore
        }
    }

    override suspend fun getFromQueue(uniqueId: UUID): Deferred<Boolean> {
        return async {
            Queue.Schema
                .selectAll()
                .where { Queue.Schema.uniqueId eq uniqueId }
                .firstOrNull()
                ?.getOrNull(Queue.Schema.vanished) ?: false
        }
    }

    override suspend fun removeFromQueue(uniqueId: UUID): Deferred<Boolean> {
        return async {
            Queue.Schema
                .deleteWhere { Queue.Schema.uniqueId eq uniqueId }
            true
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
