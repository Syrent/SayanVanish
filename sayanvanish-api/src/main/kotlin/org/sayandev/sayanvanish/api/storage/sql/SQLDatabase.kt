/*
 * This file is part of SayanVanish, licensed under the GNU General Public License v3.0.
 *
 * Copyright (c) 2026 Sayan Development and contributors
 *
 * SayanVanish is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SayanVanish is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.sayandev.sayanvanish.api.storage.sql

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.awaitAll
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.experimental.suspendedTransactionAsync
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.upsert
import org.sayandev.sayanvanish.api.Platform
import org.sayandev.sayanvanish.api.User
import org.sayandev.sayanvanish.api.VanishOptions
import org.sayandev.sayanvanish.api.VanishUser
import org.sayandev.sayanvanish.api.storage.Database
import org.sayandev.sayanvanish.api.storage.StorageConfig
import org.sayandev.stickynote.core.coroutine.dispatcher.AsyncDispatcher
import org.sayandev.stickynote.core.utils.CoroutineUtils
import java.util.*

typealias ExposedDatabase = org.jetbrains.exposed.sql.Database

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
        VanishOptions.Schema,
    )

    lateinit var database: ExposedDatabase

    override suspend fun initialize(): Deferred<Boolean> {
        transaction {
            SchemaUtils.createMissingTablesAndColumns(
                *tables.toTypedArray(),
                withLogs = false,
            )
        }
        return CompletableDeferred(true)
    }

    override suspend fun connect(): Deferred<Boolean> {
        database =
            ExposedDatabase.connect(
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
            ((VanishUser.Schema innerJoin User.Schema) innerJoin VanishOptions.Schema)
                .selectAll()
                .firstOrNull { it[VanishUser.Schema.uniqueId] == uniqueId }
                ?.let { result ->
                    VanishUser.Generic(
                        result[VanishUser.Schema.uniqueId],
                        result[User.Schema.username],
                        result[User.Schema.serverId],
                        result[VanishUser.Schema.isVanished],
                        result[User.Schema.isOnline],
                        result[VanishUser.Schema.vanishLevel],
                        optionsFromResult(result)
                    )
                }
                ?.adapt()
        }
    }

    override suspend fun getVanishUsers(): Deferred<List<VanishUser>> {
        return async {
            ((VanishUser.Schema innerJoin User.Schema) innerJoin VanishOptions.Schema)
                .selectAll()
                .map { result ->
                    // TODO: maybe add a fromResult method to VanishUser and User?
                    VanishUser.Generic(
                        result[VanishUser.Schema.uniqueId],
                        result[User.Schema.username],
                        result[User.Schema.serverId],
                        result[VanishUser.Schema.isVanished],
                        result[User.Schema.isOnline],
                        result[VanishUser.Schema.vanishLevel],
                        optionsFromResult(result)
                    ).adapt()
                }
        }
    }

    override suspend fun getUser(uniqueId: UUID): Deferred<User?> {
        return async {
            User.Schema
                .selectAll()
                .firstOrNull { it[User.Schema.uniqueId] == uniqueId }
                ?.let { result ->
                    User.Generic(
                        result[User.Schema.uniqueId],
                        result[User.Schema.username],
                        result[User.Schema.isOnline],
                        result[User.Schema.serverId]
                    )
                }
                ?.adapt()
        }
    }

    override suspend fun getUsers(): Deferred<List<User>> {
        return async {
            User.Schema
                .selectAll()
                .map { result ->
                    User.Generic(
                        result[User.Schema.uniqueId],
                        result[User.Schema.username],
                        result[User.Schema.isOnline],
                        result[User.Schema.serverId]
                    ).adapt()
                }
        }
    }

    override suspend fun saveVanishUser(vanishUser: VanishUser): Deferred<Boolean> {
        saveUser(vanishUser).await()
        return async {
            VanishUser.Schema.upsert { row ->
                row[uniqueId] = vanishUser.uniqueId
                row[isVanished] = vanishUser.isVanished
                row[vanishLevel] = vanishUser.vanishLevel
            }
            upsertOptions(vanishUser.uniqueId, vanishUser.currentOptions)
        }
    }

    override suspend fun saveUser(user: User): Deferred<Boolean> {
        return async {
            User.Schema.upsert { row ->
                row[uniqueId] = user.uniqueId
                row[username] = user.username
                row[serverId] = user.serverId
                row[isOnline] = user.isOnline
            }.insertedCount == 1
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
            VanishOptions.Schema
                .deleteWhere { VanishOptions.Schema.uniqueId eq uniqueId }
            VanishUser.Schema
                .deleteWhere { VanishUser.Schema.uniqueId eq uniqueId }
            true
        }
    }

    override suspend fun removeUser(uniqueId: UUID): Deferred<Boolean> {
        return async {
            VanishOptions.Schema
                .deleteWhere { VanishOptions.Schema.uniqueId eq uniqueId }
            VanishUser.Schema
                .deleteWhere { VanishUser.Schema.uniqueId eq uniqueId }
            User.Schema
                .deleteWhere { User.Schema.uniqueId eq uniqueId }
            true
        }
    }

    override suspend fun updateVanishUser(vanishUser: VanishUser): Deferred<Boolean> {
        return async {
            VanishUser.Schema.upsert { row ->
                row[uniqueId] = vanishUser.uniqueId
                row[isVanished] = vanishUser.isVanished
                row[vanishLevel] = vanishUser.vanishLevel
            }
            upsertOptions(vanishUser.uniqueId, vanishUser.currentOptions)
        }
    }

    override suspend fun updateUser(user: User): Deferred<Boolean> {
        return async {
            User.Schema.upsert { row ->
                row[uniqueId] = user.uniqueId
                row[username] = user.username
                row[serverId] = user.serverId
                row[isOnline] = user.isOnline
            }.insertedCount == 1
        }
    }

    override suspend fun purgeAllTables(): Deferred<Boolean> {
        tables.map { table -> async { table.deleteAll() } }.awaitAll()
        return CompletableDeferred(true)
    }

    override suspend fun purgeUsers(): Deferred<Boolean> {
        return async {
            VanishOptions.Schema.deleteAll() + VanishUser.Schema.deleteAll() + User.Schema.deleteAll() > 0
        }
    }

    override suspend fun purgeUsers(serverId: String): Deferred<Boolean> {
        return async {
            val userIds = User.Schema
                .select(User.Schema.uniqueId)
                .where { User.Schema.serverId eq serverId }
                .map { it[User.Schema.uniqueId] }

            if (userIds.isNotEmpty()) {
                (
                    VanishOptions.Schema.deleteWhere { VanishOptions.Schema.uniqueId inList userIds } +
                        VanishUser.Schema.deleteWhere { VanishUser.Schema.uniqueId inList userIds } +
                        User.Schema.deleteWhere { User.Schema.uniqueId inList userIds }
                    ) > 0
            } else {
                true
            }
        }
    }

    private fun Transaction.optionsFromResult(result: ResultRow): VanishOptions {
        return VanishOptions(
            sendMessage = result[VanishOptions.Schema.sendMessage],
            notifyStatusChangeToOthers = result[VanishOptions.Schema.notifyStatusChangeToOthers],
            notifyJoinQuitVanished = result[VanishOptions.Schema.notifyJoinQuitVanished],
            isOnJoin = result[VanishOptions.Schema.isOnJoin],
            isOnQuit = result[VanishOptions.Schema.isOnQuit],
        )
    }

    private fun Transaction.upsertOptions(uniqueId: UUID, options: VanishOptions): Boolean {
        VanishOptions.Schema.upsert { row ->
            row[VanishOptions.Schema.uniqueId] = uniqueId
            row[VanishOptions.Schema.sendMessage] = options.sendMessage
            row[VanishOptions.Schema.notifyStatusChangeToOthers] = options.notifyStatusChangeToOthers
            row[VanishOptions.Schema.notifyJoinQuitVanished] = options.notifyJoinQuitVanished
            row[VanishOptions.Schema.isOnJoin] = options.isOnJoin
            row[VanishOptions.Schema.isOnQuit] = options.isOnQuit
        }
        return true
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
