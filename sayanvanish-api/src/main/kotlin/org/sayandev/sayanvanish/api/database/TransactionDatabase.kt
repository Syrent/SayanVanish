package org.sayandev.sayanvanish.api.database

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.isActive
import org.sayandev.sayanvanish.api.User
import org.sayandev.sayanvanish.api.Platform
import org.sayandev.sayanvanish.api.PlatformAdapter
import org.sayandev.sayanvanish.api.VanishUser
import org.sayandev.sayanvanish.api.database.redis.RedisDatabase
import org.sayandev.sayanvanish.api.database.sql.SQLDatabase
import org.sayandev.stickynote.core.coroutine.dispatcher.AsyncDispatcher
import java.util.*

class TransactionDatabase: Database {

    override val dispatcher =
        AsyncDispatcher(
            "${Platform.get().pluginName.lowercase()}-transaction-thread",
            // TODO: get thread count from config
            5,
        )

    val databaseTypes = mutableMapOf<DatabaseMethod, Database>()
    var databaseConnected: Boolean = true

    override suspend fun initialize(): Deferred<Boolean> {
        val transactionMethods = databaseConfig.transactionTypes.map { it.method }.distinct()
        for (method in transactionMethods) {
            when (method) {
                DatabaseMethod.SQL -> {
                    databaseTypes[DatabaseMethod.SQL] = try {
                        SQLDatabase(databaseConfig).apply {
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
                    databaseTypes[DatabaseMethod.REDIS] = try {
                        RedisDatabase(databaseConfig).apply {
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
        }
        return CompletableDeferred(true)
    }

    override suspend fun connect(): Deferred<Boolean> {
        return CompletableDeferred(databaseConnected)
    }

    override suspend fun disconnect(): Deferred<Boolean> {
        databaseTypes.forEach { (_, database) ->
            database.disconnect()
        }
        return CompletableDeferred(true)
    }

    override suspend fun addVanishUser(vanishUser: VanishUser): Deferred<Boolean> {
        val database = database(TransactionTypes.ADD_VANISH_USER)
        return database.addVanishUser(vanishUser)
    }

    override suspend fun hasVanishUser(uniqueId: UUID): Deferred<Boolean> {
        val database = database(TransactionTypes.HAS_VANISH_USER)
        return database.hasVanishUser(uniqueId)
    }

    override suspend fun updateVanishUser(vanishUser: VanishUser): Deferred<Boolean> {
        val database = database(TransactionTypes.UPDATE_VANISH_USER)
        return database.updateVanishUser(vanishUser)
    }

    override suspend fun removeVanishUser(uniqueId: UUID): Deferred<Boolean> {
        val database = database(TransactionTypes.REMOVE_VANISH_USER)
        return database.removeVanishUser(uniqueId)
    }

    override suspend fun getVanishUser(uniqueId: UUID): Deferred<VanishUser?> {
        val database = database(TransactionTypes.GET_VANISH_USER)
        return database.getVanishUser(uniqueId)
    }

    override suspend fun getVanishUsers(): Deferred<List<VanishUser>> {
        val database = database(TransactionTypes.GET_VANISH_USERS)
        return database.getVanishUsers()
    }

    override suspend fun getUser(uniqueId: UUID): Deferred<User?> {
        val database = database(TransactionTypes.GET_USER)
        return database.getUser(uniqueId)
    }

    override suspend fun getUsers(): Deferred<List<User>> {
        val database = database(TransactionTypes.GET_USERS)
        return database.getUsers()
    }

    override suspend fun addUser(user: User): Deferred<Boolean> {
        val database = database(TransactionTypes.ADD_USER)
        return database.addUser(user)
    }

    override suspend fun hasUser(uniqueId: UUID): Deferred<Boolean> {
        val database = database(TransactionTypes.HAS_USER)
        return database.hasUser(uniqueId)
    }

    override suspend fun updateUser(user: User): Deferred<Boolean> {
        val database = database(TransactionTypes.UPDATE_USER)
        return database.updateUser(user)
    }

    override suspend fun removeUser(uniqueId: UUID): Deferred<Boolean> {
        val database = database(TransactionTypes.REMOVE_USER)
        return database.removeUser(uniqueId)
    }

    override suspend fun isInQueue(uniqueId: UUID): Deferred<Boolean> {
        val database = database(TransactionTypes.IS_IN_QUEUE)
        return database.isInQueue(uniqueId)
    }

    override suspend fun addToQueue(
        uniqueId: UUID,
        vanished: Boolean
    ): Deferred<Boolean> {
        val database = database(TransactionTypes.ADD_TO_QUEUE)
        return database.addToQueue(uniqueId, vanished)
    }

    override suspend fun getFromQueue(uniqueId: UUID): Deferred<Boolean> {
        val database = database(TransactionTypes.GET_FROM_QUEUE)
        return database.getFromQueue(uniqueId)
    }

    override suspend fun removeFromQueue(uniqueId: UUID): Deferred<Boolean> {
        val database = database(TransactionTypes.REMOVE_FROM_QUEUE)
        return database.removeFromQueue(uniqueId)
    }

    override suspend fun purgeAllTables(): Deferred<Boolean> {
        return CompletableDeferred<Boolean>().apply {
            databaseTypes.values.map { database ->
                async {
                    database.purgeAllTables()
                }
            }.awaitAll()
            complete(true)
        }
    }

    override suspend fun purgeUsers(): Deferred<Boolean> {
        return CompletableDeferred<Boolean>().apply {
            databaseTypes.values.map { database ->
                async {
                    database.purgeUsers()
                }
            }.awaitAll()
            complete(true)
        }
    }

    override suspend fun purgeUsers(serverId: String): Deferred<Boolean> {
        return CompletableDeferred<Boolean>().apply {
            databaseTypes.values.map { database ->
                async {
                    database.purgeUsers(serverId)
                }
            }.awaitAll()
            complete(true)
        }
    }

    fun database(method: DatabaseMethod): Database {
        return databaseTypes[method] ?: let {
            val (fallbackMethod, fallbackDatabase) = databaseTypes.entries.first()
            Platform.Companion.get().logger.warning("Tried to get a database of type $method, but it was not initialized. falling back to ${fallbackMethod} database method.")
            fallbackDatabase
        }
    }

    fun database(transactionType: TransactionType): Database {
        return database(transactionType.method)
    }

    private fun logDatabaseError() {
        Platform.get().logger.severe("Database connection failed. Disabling the plugin.")
        Platform.get().logger.severe("Please check the following:")
        Platform.get().logger.severe("- Make sure your database server is not misconfigured.")
        Platform.get().logger.severe("- Make sure your database server is running.")
        Platform.get().logger.severe("Here's the full error trace:")
    }


    fun <T> async(
        block: suspend CoroutineScope.() -> T
    ): Deferred<T> {
        val session = CoroutineScope(dispatcher)
        if (!session.isActive) {
            return CompletableDeferred<T>().apply { cancel() }
        }

        return session.async(dispatcher, CoroutineStart.DEFAULT, block)
    }

}