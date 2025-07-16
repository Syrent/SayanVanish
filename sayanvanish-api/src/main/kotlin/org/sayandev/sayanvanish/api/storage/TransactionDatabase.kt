package org.sayandev.sayanvanish.api.storage

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.isActive
import org.sayandev.sayanvanish.api.User
import org.sayandev.sayanvanish.api.Platform
import org.sayandev.sayanvanish.api.VanishUser
import org.sayandev.sayanvanish.api.storage.redis.RedisDatabase
import org.sayandev.sayanvanish.api.storage.sql.SQLDatabase
import org.sayandev.stickynote.core.coroutine.dispatcher.AsyncDispatcher
import java.util.*

class TransactionDatabase: Database {

    override var connected = false

    override val dispatcher =
        AsyncDispatcher(
            "${Platform.get().pluginName.lowercase()}-transaction-thread",
            storageConfig.transactionThreadCount,
        )

    val databaseTypes = mutableMapOf<DatabaseType, Database>()
    var databaseConnected: Boolean = true

    override suspend fun initialize(): Deferred<Boolean> {
        val transactionMethods = storageConfig.transactionTypes.map { it.type }.distinct()
        for (method in transactionMethods) {
            when (method) {
                DatabaseType.SQL -> {
                    databaseTypes[DatabaseType.SQL] = try {
                        SQLDatabase(storageConfig).also { sqlDatabase ->
                            sqlDatabase.connect()
                            sqlDatabase.initialize()
                        }
                    } catch (e: Exception) {
                        databaseConnected = false
                        logDatabaseError()
                        throw e
                    }
                }
                DatabaseType.REDIS -> {
                    databaseTypes[DatabaseType.REDIS] = try {
                        RedisDatabase(storageConfig).also { redisDatabase ->
                            redisDatabase.initialize()
                            redisDatabase.connect()
                        }
                    } catch (e: Exception) {
                        databaseConnected = false
                        logDatabaseError()
                        throw e
                    }
                }
            }
        }
        connected = databaseTypes.values.all { it.connected }
        return CompletableDeferred(connected)
    }

    inline fun <reified D: Database> getByType(): D {
        return databaseTypes.values.filterIsInstance<D>().firstOrNull()
            ?: throw IllegalArgumentException("Received database with type `${D::class.simpleName}` but it isn't registered in the TransactionDatabase database types.")
    }

    override suspend fun connect(): Deferred<Boolean> {
        return CompletableDeferred(connected)
    }

    override suspend fun disconnect(): Deferred<Boolean> {
        databaseTypes.forEach { (_, database) ->
            database.disconnect()
        }
        connected = false
        return CompletableDeferred(connected)
    }

    override suspend fun saveVanishUser(vanishUser: VanishUser): Deferred<Boolean> {
        val database = database(TransactionTypes.VANISH_USER)
        return database.saveVanishUser(vanishUser)
    }

    override suspend fun hasVanishUser(uniqueId: UUID): Deferred<Boolean> {
        val database = database(TransactionTypes.VANISH_USER)
        return database.hasVanishUser(uniqueId)
    }

    override suspend fun updateVanishUser(vanishUser: VanishUser): Deferred<Boolean> {
        val database = database(TransactionTypes.VANISH_USER)
        return database.updateVanishUser(vanishUser)
    }

    override suspend fun removeVanishUser(uniqueId: UUID): Deferred<Boolean> {
        val database = database(TransactionTypes.VANISH_USER)
        return database.removeVanishUser(uniqueId)
    }

    override suspend fun getVanishUser(uniqueId: UUID): Deferred<VanishUser?> {
        val database = database(TransactionTypes.VANISH_USER)
        return database.getVanishUser(uniqueId)
    }

    override suspend fun getVanishUsers(): Deferred<List<VanishUser>> {
        val database = database(TransactionTypes.VANISH_USER)
        return database.getVanishUsers()
    }

    override suspend fun getUser(uniqueId: UUID): Deferred<User?> {
        val database = database(TransactionTypes.USER)
        return database.getUser(uniqueId)
    }

    override suspend fun getUsers(): Deferred<List<User>> {
        val database = database(TransactionTypes.USER)
        return database.getUsers()
    }

    override suspend fun saveUser(user: User): Deferred<Boolean> {
        val database = database(TransactionTypes.USER)
        return database.saveUser(user)
    }

    override suspend fun hasUser(uniqueId: UUID): Deferred<Boolean> {
        val database = database(TransactionTypes.USER)
        return database.hasUser(uniqueId)
    }

    override suspend fun updateUser(user: User): Deferred<Boolean> {
        val database = database(TransactionTypes.USER)
        return database.updateUser(user)
    }

    override suspend fun removeUser(uniqueId: UUID): Deferred<Boolean> {
        val database = database(TransactionTypes.USER)
        return database.removeUser(uniqueId)
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

    fun database(method: DatabaseType): Database {
        return databaseTypes[method] ?: let {
            val (fallbackMethod, fallbackDatabase) = databaseTypes.entries.first()
            Platform.Companion.get().logger.warning("Tried to get a database of type $method, but it was not initialized. falling back to ${fallbackMethod} database method.")
            fallbackDatabase
        }
    }

    fun database(transactionType: TransactionType): Database {
        return database(transactionType.type)
    }

    private fun logDatabaseError() {
        Platform.get().logger.severe("Database connection failed. Disabling the plugin...")
        Platform.get().logger.severe("Please check the following:")
        Platform.get().logger.severe("- Make sure your database server is not misconfigured.")
        Platform.get().logger.severe("- Make sure your database server is running.")
        Platform.get().logger.severe("Your active database types:")
        for (type in databaseTypes) {
            Platform.get().logger.severe("- ${type.key.name} (${type.value::class.simpleName})")
        }
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