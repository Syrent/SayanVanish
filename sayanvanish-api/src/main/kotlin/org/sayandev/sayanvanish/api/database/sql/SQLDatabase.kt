package org.sayandev.sayanvanish.api.database.sql

import kotlinx.coroutines.*
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.sayandev.sayanvanish.api.BasicUser
import org.sayandev.sayanvanish.api.Platform
import org.sayandev.sayanvanish.api.User
import org.sayandev.sayanvanish.api.User.Companion.convert
import org.sayandev.sayanvanish.api.VanishOptions
import org.sayandev.sayanvanish.api.database.Database
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import kotlin.reflect.safeCast
import org.jetbrains.exposed.v1.jdbc.Database as ExposedDatabase

class SQLDatabase<U: User>(
    val config: SQLConfig,
    val type: Class<out User>,
    override var useCache: Boolean = true
) : Database<U> {

    override var cache: MutableMap<UUID, U> = ConcurrentHashMap()
    var basicCache = ConcurrentHashMap<UUID, BasicUser>()

    private val executor = Executors.newFixedThreadPool(config.poolProperties.maximumPoolSize)
    private val dispatcher: ExecutorCoroutineDispatcher = executor.asCoroutineDispatcher()
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    private val tables = SqlTables(config.tablePrefix)
    private var exposedDatabase: ExposedDatabase? = null

    override fun initialize() {
        runBlocking {
            dbQuery {
                SchemaUtils.create(tables.users, tables.basicUsers, tables.queue)
            }
        }
    }

    override fun connect() {
        cache.clear()
        basicCache.clear()
        exposedDatabase = connectDatabase().also { TransactionManager.defaultDatabase = it }
    }

    override fun disconnect() {
        exposedDatabase?.let { TransactionManager.closeAndUnregister(it) }
        exposedDatabase = null
        cache.clear()
        basicCache.clear()
        scope.cancel()
        dispatcher.close()
        executor.shutdown()
    }

    override fun getUser(uniqueId: UUID, useCache: Boolean): U? {
        if (this.useCache && useCache) {
            val cacheUser = cache[uniqueId]
            if (cacheUser == null) return null
            return (type.kotlin.safeCast(cacheUser) as? U) ?: (cacheUser.convert(type) as U)
        }

        val user = runBlocking { getUserSuspended(uniqueId, false) }
        if (user != null) cache[uniqueId] = user
        return user
    }

    override fun getUsersAsync(result: (List<U>) -> Unit) {
        val deferred = scope.async { getUsersFromDatabase() }
        deferred.invokeOnCompletion { throwable ->
            if (throwable != null) {
                Platform.get().logger.severe("Failed to fetch users from database (getUsersAsync)")
                throwable.printStackTrace()
                result(emptyList())
                return@invokeOnCompletion
            }
            result(deferred.getCompleted())
        }
    }

    override fun getUsers(): List<U> {
        if (useCache) return cache.values.toList()
        return runBlocking { getUsersFromDatabase() }
    }

    override fun getBasicUsers(useCache: Boolean): List<BasicUser> {
        if (useCache) return basicCache.values.toList()
        return runBlocking { getBasicUsersFromDatabase() }
    }

    override fun getBasicUsersAsync(result: (List<BasicUser>) -> Unit) {
        val deferred = scope.async { getBasicUsersFromDatabase() }
        deferred.invokeOnCompletion { throwable ->
            if (throwable != null) {
                Platform.get().logger.severe("Failed to fetch users from database (getBasicUsersAsync)")
                throwable.printStackTrace()
                result(emptyList())
                return@invokeOnCompletion
            }
            result(deferred.getCompleted())
        }
    }

    override fun addUser(user: U) {
        runBlocking { addUserSuspended(user) }
    }

    override fun addBasicUser(user: BasicUser) {
        runBlocking { addBasicUserSuspended(user) }
    }

    override fun hasUser(uniqueId: UUID): Boolean {
        return runBlocking { hasUserSuspended(uniqueId) }
    }

    override fun hasBasicUser(uniqueId: UUID, useCache: Boolean): Boolean {
        if (useCache) return basicCache.contains(uniqueId)
        return runBlocking { hasBasicUserSuspended(uniqueId) }
    }

    override fun removeUser(uniqueId: UUID) {
        runBlocking { removeUserSuspended(uniqueId) }
    }

    override fun removeBasicUser(uniqueId: UUID) {
        runBlocking { removeBasicUserSuspended(uniqueId) }
    }

    override fun updateUser(user: U) {
        runBlocking { updateUserSuspended(user) }
    }

    override fun updateBasicUser(user: BasicUser) {
        runBlocking { updateBasicUserSuspended(user) }
    }

    override fun isInQueue(uniqueId: UUID, result: (Boolean) -> Unit) {
        val deferred = scope.async { isInQueueSuspended(uniqueId) }
        deferred.invokeOnCompletion { throwable ->
            if (throwable != null) {
                Platform.get().logger.severe("Failed to fetch is in queue from database (isInQueue)")
                throwable.printStackTrace()
                result(false)
                return@invokeOnCompletion
            }
            result(deferred.getCompleted())
        }
    }

    override fun addToQueue(uniqueId: UUID, vanished: Boolean) {
        scope.launch {
            try {
                addToQueueSuspended(uniqueId, vanished)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun getFromQueue(uniqueId: UUID, result: (Boolean) -> Unit) {
        val deferred = scope.async { getFromQueueSuspended(uniqueId) }
        deferred.invokeOnCompletion { throwable ->
            if (throwable != null) {
                Platform.get().logger.severe("Failed to fetch queue from database (getFromQueue)")
                throwable.printStackTrace()
                result(false)
                return@invokeOnCompletion
            }
            result(deferred.getCompleted())
        }
    }

    override fun removeFromQueue(uniqueId: UUID) {
        scope.launch {
            try {
                removeFromQueueSuspended(uniqueId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun purgeCache() {
        cache.clear()
        basicCache.clear()
    }

    override fun purge() {
        runBlocking { purgeSuspended() }
    }

    override fun purgeBasic() {
        runBlocking { purgeBasicSuspended() }
    }

    override fun purgeBasic(serverId: String) {
        runBlocking { purgeBasicSuspended(serverId) }
    }

    suspend fun getUserSuspended(uniqueId: UUID, useCache: Boolean = true): U? {
        if (this.useCache && useCache) {
            val cacheUser = cache[uniqueId]
            if (cacheUser == null) return null
            return (type.kotlin.safeCast(cacheUser) as? U) ?: (cacheUser.convert(type) as U)
        }

        val user = dbQuery {
            tables.users
                .selectAll()
                .where { tables.users.uniqueId eq uniqueId.toString() }
                .firstOrNull()
                ?.toUser()
        }
        if (user != null) cache[uniqueId] = user
        return user
    }

    fun getUserDeferred(uniqueId: UUID, useCache: Boolean = true): Deferred<U?> =
        scope.async { getUserSuspended(uniqueId, useCache) }

    suspend fun getUsersSuspended(useCache: Boolean = this.useCache): List<U> {
        if (useCache) return cache.values.toList()
        return getUsersFromDatabase()
    }

    fun getUsersDeferred(useCache: Boolean = this.useCache): Deferred<List<U>> =
        scope.async { getUsersSuspended(useCache) }

    suspend fun getBasicUsersSuspended(useCache: Boolean = true): List<BasicUser> {
        if (useCache) return basicCache.values.toList()
        return getBasicUsersFromDatabase()
    }

    fun getBasicUsersDeferred(useCache: Boolean = true): Deferred<List<BasicUser>> =
        scope.async { getBasicUsersSuspended(useCache) }

    suspend fun addUserSuspended(user: U) {
        cache[user.uniqueId] = user
        dbQuery {
            val exists =
                tables.users
                    .selectAll()
                    .where { tables.users.uniqueId eq user.uniqueId.toString() }
                    .limit(1)
                    .count() > 0
            if (!exists) {
                tables.users.insert { row ->
                    row[tables.users.uniqueId] = user.uniqueId.toString()
                    row[tables.users.username] = user.username
                    row[tables.users.serverId] = user.serverId
                    row[tables.users.isVanished] = boolInt(user.isVanished)
                    row[tables.users.isOnline] = boolInt(user.isOnline)
                    row[tables.users.vanishLevel] = user.vanishLevel
                }
            } else {
                tables.users.update({ tables.users.uniqueId eq user.uniqueId.toString() }) { row ->
                    row[tables.users.username] = user.username
                    row[tables.users.isVanished] = boolInt(user.isVanished)
                    row[tables.users.isOnline] = boolInt(user.isOnline)
                    row[tables.users.vanishLevel] = user.vanishLevel
                }
            }
        }
    }

    fun addUserDeferred(user: U): Deferred<Unit> =
        scope.async { addUserSuspended(user) }

    suspend fun addBasicUserSuspended(user: BasicUser) {
        basicCache[user.uniqueId] = user
        dbQuery {
            val exists =
                tables.basicUsers
                    .selectAll()
                    .where { tables.basicUsers.uniqueId eq user.uniqueId.toString() }
                    .limit(1)
                    .count() > 0
            if (!exists) {
                tables.basicUsers.insert { row ->
                    row[tables.basicUsers.uniqueId] = user.uniqueId.toString()
                    row[tables.basicUsers.username] = user.username
                    row[tables.basicUsers.serverId] = user.serverId
                }
            } else if (user.serverId != Platform.get().serverId) {
                tables.basicUsers.update({ tables.basicUsers.uniqueId eq user.uniqueId.toString() }) { row ->
                    row[tables.basicUsers.username] = user.username
                    row[tables.basicUsers.serverId] = user.serverId
                }
            }
        }
    }

    fun addBasicUserDeferred(user: BasicUser): Deferred<Unit> =
        scope.async { addBasicUserSuspended(user) }

    suspend fun hasUserSuspended(uniqueId: UUID): Boolean =
        dbQuery {
            tables.users
                .selectAll()
                .where { tables.users.uniqueId eq uniqueId.toString() }
                .limit(1)
                .count() > 0
        }

    fun hasUserDeferred(uniqueId: UUID): Deferred<Boolean> =
        scope.async { hasUserSuspended(uniqueId) }

    suspend fun hasBasicUserSuspended(uniqueId: UUID): Boolean =
        dbQuery {
            tables.basicUsers
                .selectAll()
                .where { tables.basicUsers.uniqueId eq uniqueId.toString() }
                .limit(1)
                .count() > 0
        }

    fun hasBasicUserDeferred(uniqueId: UUID): Deferred<Boolean> =
        scope.async { hasBasicUserSuspended(uniqueId) }

    suspend fun removeUserSuspended(uniqueId: UUID) {
        cache.remove(uniqueId)
        dbQuery {
            tables.users.deleteWhere { tables.users.uniqueId eq uniqueId.toString() }
        }
    }

    fun removeUserDeferred(uniqueId: UUID): Deferred<Unit> =
        scope.async { removeUserSuspended(uniqueId) }

    suspend fun removeBasicUserSuspended(uniqueId: UUID) {
        basicCache.remove(uniqueId)
        dbQuery {
            tables.basicUsers.deleteWhere { tables.basicUsers.uniqueId eq uniqueId.toString() }
        }
    }

    fun removeBasicUserDeferred(uniqueId: UUID): Deferred<Unit> =
        scope.async { removeBasicUserSuspended(uniqueId) }

    suspend fun updateUserSuspended(user: U) {
        cache[user.uniqueId] = user
        dbQuery {
            tables.users.update({ tables.users.uniqueId eq user.uniqueId.toString() }) { row ->
                row[tables.users.username] = user.username
                row[tables.users.isVanished] = boolInt(user.isVanished)
                row[tables.users.isOnline] = boolInt(user.isOnline)
                row[tables.users.vanishLevel] = user.vanishLevel
            }
        }
    }

    fun updateUserDeferred(user: U): Deferred<Unit> =
        scope.async { updateUserSuspended(user) }

    suspend fun updateBasicUserSuspended(user: BasicUser) {
        basicCache[user.uniqueId] = user
        dbQuery {
            tables.basicUsers.update({ tables.basicUsers.uniqueId eq user.uniqueId.toString() }) { row ->
                row[tables.basicUsers.username] = user.username
                row[tables.basicUsers.serverId] = user.serverId
            }
        }
    }

    fun updateBasicUserDeferred(user: BasicUser): Deferred<Unit> =
        scope.async { updateBasicUserSuspended(user) }

    suspend fun isInQueueSuspended(uniqueId: UUID): Boolean =
        dbQuery {
            tables.queue
                .selectAll()
                .where { tables.queue.uniqueId eq uniqueId.toString() }
                .limit(1)
                .count() > 0
        }

    fun isInQueueDeferred(uniqueId: UUID): Deferred<Boolean> =
        scope.async { isInQueueSuspended(uniqueId) }

    suspend fun addToQueueSuspended(uniqueId: UUID, vanished: Boolean) {
        dbQuery {
            val exists =
                tables.queue
                    .selectAll()
                    .where { tables.queue.uniqueId eq uniqueId.toString() }
                    .limit(1)
                    .count() > 0
            if (!exists) {
                tables.queue.insert { row ->
                    row[tables.queue.uniqueId] = uniqueId.toString()
                    row[tables.queue.vanishedValue] = vanished.toString()
                }
            } else {
                tables.queue.update({ tables.queue.uniqueId eq uniqueId.toString() }) { row ->
                    row[tables.queue.vanishedValue] = vanished.toString()
                }
            }
        }
    }

    fun addToQueueDeferred(uniqueId: UUID, vanished: Boolean): Deferred<Unit> =
        scope.async { addToQueueSuspended(uniqueId, vanished) }

    suspend fun getFromQueueSuspended(uniqueId: UUID): Boolean =
        dbQuery {
            tables.queue
                .selectAll()
                .where { tables.queue.uniqueId eq uniqueId.toString() }
                .firstOrNull()
                ?.get(tables.queue.vanishedValue)
                ?.toBoolean()
                ?: false
        }

    fun getFromQueueDeferred(uniqueId: UUID): Deferred<Boolean> =
        scope.async { getFromQueueSuspended(uniqueId) }

    suspend fun removeFromQueueSuspended(uniqueId: UUID) {
        dbQuery {
            tables.queue.deleteWhere { tables.queue.uniqueId eq uniqueId.toString() }
        }
    }

    fun removeFromQueueDeferred(uniqueId: UUID): Deferred<Unit> =
        scope.async { removeFromQueueSuspended(uniqueId) }

    suspend fun purgeSuspended() {
        dbQuery {
            tables.users.deleteAll()
            tables.basicUsers.deleteAll()
            tables.queue.deleteAll()
        }
    }

    fun purgeDeferred(): Deferred<Unit> =
        scope.async { purgeSuspended() }

    suspend fun purgeBasicSuspended() {
        dbQuery {
            tables.basicUsers.deleteAll()
            tables.queue.deleteAll()
        }
    }

    fun purgeBasicDeferred(): Deferred<Unit> =
        scope.async { purgeBasicSuspended() }

    suspend fun purgeBasicSuspended(serverId: String) {
        dbQuery {
            tables.basicUsers.deleteWhere { tables.basicUsers.serverId eq serverId }
        }
    }

    fun purgeBasicDeferred(serverId: String): Deferred<Unit> =
        scope.async { purgeBasicSuspended(serverId) }

    private suspend fun getUsersFromDatabase(): List<U> =
        dbQuery {
            tables.users
                .selectAll()
                .map { it.toUser() }
        }

    private suspend fun getBasicUsersFromDatabase(): List<BasicUser> =
        dbQuery {
            tables.basicUsers
                .selectAll()
                .map { it.toBasicUser() }
        }

    private suspend fun <T> dbQuery(statement: suspend org.jetbrains.exposed.v1.core.Transaction.() -> T): T {
        val db = requireNotNull(exposedDatabase) { "Database not connected." }
        return withContext(dispatcher) { suspendTransaction(db = db, statement = statement) }
    }

    private fun connectDatabase(): ExposedDatabase {
        return ExposedDatabase.connect(
            url =
                when (config.method) {
                    SQLConfig.SQLMethod.SQLITE -> "jdbc:sqlite:${Platform.get().rootDirectory.absolutePath}/database"
                    SQLConfig.SQLMethod.MARIADB -> "jdbc:mariadb://${config.host}:${config.port}/${config.database}?autoReconnect=true&rewriteBatchedStatements=true&useSSL=${config.poolProperties.useSSL}"
                    SQLConfig.SQLMethod.MYSQL -> "jdbc:mysql://${config.host}:${config.port}/${config.database}?autoReconnect=true&rewriteBatchedStatements=true&useSSL=${config.poolProperties.useSSL}"
                },
            driver =
                when (config.method) {
                    SQLConfig.SQLMethod.SQLITE -> "org.sqlite.JDBC"
                    SQLConfig.SQLMethod.MARIADB -> "org.mariadb.jdbc.Driver"
                    SQLConfig.SQLMethod.MYSQL -> "com.mysql.cj.jdbc.Driver"
                },
            user = config.username,
            password = config.password,
        )
    }

    private fun ResultRow.toUser(): U {
        val user = object : User {
            override val uniqueId: UUID = UUID.fromString(this@toUser[tables.users.uniqueId])
            override var username: String = this@toUser[tables.users.username]
            override var serverId: String = this@toUser[tables.users.serverId]
            override var currentOptions: VanishOptions = VanishOptions.defaultOptions()

            override var isVanished: Boolean = this@toUser[tables.users.isVanished] != 0
            override var isOnline: Boolean = this@toUser[tables.users.isOnline] != 0
            override var vanishLevel: Int = this@toUser[tables.users.vanishLevel]
        }
        return (type.kotlin.safeCast(user) as? U) ?: (user.convert(type) as U)
    }

    private fun ResultRow.toBasicUser(): BasicUser =
        BasicUser.create(
            UUID.fromString(this[tables.basicUsers.uniqueId]),
            this[tables.basicUsers.username],
            this[tables.basicUsers.serverId]
        )

    private fun boolInt(value: Boolean): Int = if (value) 1 else 0

    private class SqlTables(prefix: String) {
        val users = UsersTable("${prefix}users")
        val basicUsers = BasicUsersTable("${prefix}basic_users")
        val queue = QueueTable("${prefix}queue")
    }

    private class UsersTable(name: String) : Table(name) {
        val uniqueId = varchar("UUID", 64)
        val username = varchar("username", 16)
        val serverId = varchar("server", 128)
        val isVanished = integer("is_vanished")
        val isOnline = integer("is_online")
        val vanishLevel = integer("vanish_level")

        override val primaryKey = PrimaryKey(uniqueId)
    }

    private class BasicUsersTable(name: String) : Table(name) {
        val uniqueId = varchar("UUID", 64)
        val username = varchar("username", 16)
        val serverId = varchar("server", 128)

        override val primaryKey = PrimaryKey(uniqueId)
    }

    private class QueueTable(name: String) : Table(name) {
        val uniqueId = varchar("UUID", 64)
        val vanishedValue = varchar("vanished", 16)

        override val primaryKey = PrimaryKey(uniqueId)
    }
}
