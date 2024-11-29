package org.sayandev.sayanvanish.api.database

import kotlinx.coroutines.Deferred
import org.sayandev.sayanvanish.api.BasicUser
import org.sayandev.sayanvanish.api.User
import java.util.*

interface DatabaseTemplate {
    suspend fun initialize()

    suspend fun connect(): Deferred<Boolean>

    suspend fun disconnect(): Deferred<Boolean>

    suspend fun addUser(user: User): Deferred<Boolean>

    suspend fun hasUser(uniqueId: UUID): Deferred<Boolean>

    suspend fun updateUser(user: User): Deferred<Boolean>

    suspend fun removeUser(uniqueId: UUID): Deferred<Boolean>

    suspend fun getUser(uniqueId: UUID): Deferred<User?>
    suspend fun <U: User> getUser(uniqueId: UUID): Deferred<U?>

    suspend fun getUsers(): Deferred<Collection<User>>
    suspend fun <U: User> getUsers(): Deferred<Collection<U>>

    suspend fun getBasicUsers(): Deferred<List<BasicUser>>

    suspend fun addBasicUser(user: BasicUser): Deferred<Boolean>
    fun hasBasicUser(uniqueId: UUID, useCache: Boolean): Deferred<Boolean>
    fun updateBasicUser(user: BasicUser): Deferred<Boolean>
    fun removeBasicUser(uniqueId: UUID): Deferred<Boolean>

    fun isInQueue(uniqueId: UUID): Deferred<Boolean>
    fun addToQueue(uniqueId: UUID, vanished: Boolean): Deferred<Boolean>
    fun getFromQueue(uniqueId: UUID): Deferred<Boolean>
    fun removeFromQueue(uniqueId: UUID): Deferred<Boolean>

    fun purge(): Deferred<Boolean>
    fun purgeCache(): Deferred<Boolean>
    fun purgeBasic(): Deferred<Boolean>
    fun purgeBasic(serverId: String): Deferred<Boolean>
}