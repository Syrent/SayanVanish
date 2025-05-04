package org.sayandev.sayanvanish.api.database

import kotlinx.coroutines.Deferred
import org.sayandev.sayanvanish.api.User
import org.sayandev.sayanvanish.api.VanishUser
import java.util.*

interface Database {

    suspend fun initialize(): Deferred<Boolean>

    suspend fun connect(): Deferred<Boolean>

    suspend fun disconnect(): Deferred<Boolean>

    suspend fun addVanishUser(vanishUser: VanishUser): Deferred<Boolean>

    suspend fun hasVanishUser(uniqueId: UUID): Deferred<Boolean>

    suspend fun updateVanishUser(vanishUser: VanishUser): Deferred<Boolean>

    suspend fun removeVanishUser(uniqueId: UUID): Deferred<Boolean>

    suspend fun getVanishUser(uniqueId: UUID): Deferred<VanishUser?>

    suspend fun getVanishUsers(): Deferred<List<VanishUser>>

    suspend fun getUsers(): Deferred<List<User>>

    suspend fun addUser(user: User): Deferred<Boolean>
    suspend fun hasUser(uniqueId: UUID): Deferred<Boolean>
    suspend fun updateUser(user: User): Deferred<Boolean>
    suspend fun removeUser(uniqueId: UUID): Deferred<Boolean>

    suspend fun isInQueue(uniqueId: UUID): Deferred<Boolean>
    suspend fun addToQueue(uniqueId: UUID, vanished: Boolean): Deferred<Boolean>
    suspend fun getFromQueue(uniqueId: UUID): Deferred<Boolean>
    suspend fun removeFromQueue(uniqueId: UUID): Deferred<Boolean>

    suspend fun purgeAllTables(): Deferred<Boolean>
    suspend fun purgeUsers(): Deferred<Boolean>
}