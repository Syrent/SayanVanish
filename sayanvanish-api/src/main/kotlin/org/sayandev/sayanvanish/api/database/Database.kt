package org.sayandev.sayanvanish.api.database

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.future.asCompletableFuture
import kotlinx.coroutines.runBlocking
import org.sayandev.sayanvanish.api.User
import org.sayandev.sayanvanish.api.VanishUser
import org.sayandev.stickynote.core.coroutine.dispatcher.AsyncDispatcher
import org.sayandev.stickynote.core.utils.async
import java.util.*
import java.util.concurrent.CompletableFuture

interface Database {

    val dispatcher: AsyncDispatcher

    suspend fun initialize(): Deferred<Boolean>

    suspend fun connect(): Deferred<Boolean>

    suspend fun disconnect(): Deferred<Boolean>

    suspend fun addVanishUser(vanishUser: VanishUser): Deferred<Boolean>

    fun addVanishUserFuture(vanishUser: VanishUser): CompletableFuture<Boolean> {
        return async(dispatcher) { addVanishUser(vanishUser).await() }.asCompletableFuture()
    }

    fun addVanishUserSync(vanishUser: VanishUser): Boolean {
        return runBlocking { addVanishUser(vanishUser).await() }
    }

    suspend fun hasVanishUser(uniqueId: UUID): Deferred<Boolean>

    fun hasVanishUserFuture(uniqueId: UUID): CompletableFuture<Boolean> {
        return async(dispatcher) { hasVanishUser(uniqueId).await() }.asCompletableFuture()
    }

    fun hasVanishUserSync(uniqueId: UUID): Boolean {
        return runBlocking { hasVanishUser(uniqueId).await() }
    }

    suspend fun updateVanishUser(vanishUser: VanishUser): Deferred<Boolean>

    fun updateVanishUserFuture(vanishUser: VanishUser): CompletableFuture<Boolean> {
        return async(dispatcher) { updateVanishUser(vanishUser).await() }.asCompletableFuture()
    }

    fun updateVanishUserSync(vanishUser: VanishUser): Boolean {
        return runBlocking { updateVanishUser(vanishUser).await() }
    }

    suspend fun removeVanishUser(uniqueId: UUID): Deferred<Boolean>

    fun removeVanishUserFuture(uniqueId: UUID): CompletableFuture<Boolean> {
        return async(dispatcher) { removeVanishUser(uniqueId).await() }.asCompletableFuture()
    }

    fun removeVanishUserSync(uniqueId: UUID): Boolean {
        return runBlocking { removeVanishUser(uniqueId).await() }
    }

    suspend fun getVanishUser(uniqueId: UUID): Deferred<VanishUser?>

    fun getVanishUserFuture(uniqueId: UUID): CompletableFuture<VanishUser?> {
        return async(dispatcher) { getVanishUser(uniqueId).await() }.asCompletableFuture()
    }

    fun getVanishUserSync(uniqueId: UUID): VanishUser? {
        return runBlocking { getVanishUser(uniqueId).await() }
    }

    suspend fun getVanishUsers(): Deferred<List<VanishUser>>

    fun getVanishUsersFuture(): CompletableFuture<List<VanishUser>> {
        return async(dispatcher) { getVanishUsers().await() }.asCompletableFuture()
    }

    fun getVanishUsersSync(): List<VanishUser> {
        return runBlocking { getVanishUsers().await() }
    }

    suspend fun getUsers(): Deferred<List<User>>

    fun getUsersFuture(): CompletableFuture<List<User>> {
        return async(dispatcher) { getUsers().await() }.asCompletableFuture()
    }

    fun getUsersSync(): List<User> {
        return runBlocking { getUsers().await() }
    }

    suspend fun addUser(user: User): Deferred<Boolean>

    fun addUserFuture(user: User): CompletableFuture<Boolean> {
        return async(dispatcher) { addUser(user).await() }.asCompletableFuture()
    }

    fun addUserSync(user: User): Boolean {
        return runBlocking { addUser(user).await() }
    }

    suspend fun hasUser(uniqueId: UUID): Deferred<Boolean>

    fun hasUserFuture(uniqueId: UUID): CompletableFuture<Boolean> {
        return async(dispatcher) { hasUser(uniqueId).await() }.asCompletableFuture()
    }

    fun hasUserSync(uniqueId: UUID): Boolean {
        return runBlocking { hasUser(uniqueId).await() }
    }

    suspend fun updateUser(user: User): Deferred<Boolean>

    fun updateUserFuture(user: User): CompletableFuture<Boolean> {
        return async(dispatcher) { updateUser(user).await() }.asCompletableFuture()
    }

    fun updateUserSync(user: User): Boolean {
        return runBlocking { updateUser(user).await() }
    }

    suspend fun removeUser(uniqueId: UUID): Deferred<Boolean>

    fun removeUserFuture(uniqueId: UUID): CompletableFuture<Boolean> {
        return async(dispatcher) { removeUser(uniqueId).await() }.asCompletableFuture()
    }

    fun removeUserSync(uniqueId: UUID): Boolean {
        return runBlocking { removeUser(uniqueId).await() }
    }

    suspend fun isInQueue(uniqueId: UUID): Deferred<Boolean>

    fun isInQueueFuture(uniqueId: UUID): CompletableFuture<Boolean> {
        return async(dispatcher) { isInQueue(uniqueId).await() }.asCompletableFuture()
    }

    fun isInQueueSync(uniqueId: UUID): Boolean {
        return runBlocking { isInQueue(uniqueId).await() }
    }

    suspend fun addToQueue(uniqueId: UUID, vanished: Boolean): Deferred<Boolean>

    fun addToQueueFuture(uniqueId: UUID, vanished: Boolean): CompletableFuture<Boolean> {
        return async(dispatcher) { addToQueue(uniqueId, vanished).await() }.asCompletableFuture()
    }

    fun addToQueueSync(uniqueId: UUID, vanished: Boolean): Boolean {
        return runBlocking { addToQueue(uniqueId, vanished).await() }
    }

    suspend fun getFromQueue(uniqueId: UUID): Deferred<Boolean>

    fun getFromQueueFuture(uniqueId: UUID): CompletableFuture<Boolean> {
        return async(dispatcher) { getFromQueue(uniqueId).await() }.asCompletableFuture()
    }

    fun getFromQueueSync(uniqueId: UUID): Boolean {
        return runBlocking { getFromQueue(uniqueId).await() }
    }

    suspend fun removeFromQueue(uniqueId: UUID): Deferred<Boolean>

    fun removeFromQueueFuture(uniqueId: UUID): CompletableFuture<Boolean> {
        return async(dispatcher) { removeFromQueue(uniqueId).await() }.asCompletableFuture()
    }

    fun removeFromQueueSync(uniqueId: UUID): Boolean {
        return runBlocking { removeFromQueue(uniqueId).await() }
    }

    suspend fun purgeAllTables(): Deferred<Boolean>

    fun purgeAllTablesFuture(): CompletableFuture<Boolean> {
        return async(dispatcher) { purgeAllTables().await() }.asCompletableFuture()
    }

    suspend fun purgeUsers(): Deferred<Boolean>

    fun purgeUsersFuture(): CompletableFuture<Boolean> {
        return async(dispatcher) { purgeUsers().await() }.asCompletableFuture()
    }

    suspend fun purgeUsers(serverId: String): Deferred<Boolean>

    fun purgeUsersFuture(serverId: String): CompletableFuture<Boolean> {
        return async(dispatcher) { purgeUsers(serverId).await() }.asCompletableFuture()
    }
}