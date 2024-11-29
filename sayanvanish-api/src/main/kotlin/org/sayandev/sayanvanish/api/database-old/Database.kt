package org.sayandev.sayanvanish.api.`database-old`

import org.sayandev.sayanvanish.api.BasicUser
import org.sayandev.sayanvanish.api.User
import java.util.*

interface Database<U: User> {

    var cache: MutableMap<UUID, U>
    var useCache: Boolean

    fun initialize()

    fun connect()

    fun disconnect()

    fun addUser(user: U)

    fun hasUser(uniqueId: UUID): Boolean

    fun updateUser(user: U)

    fun removeUser(uniqueId: UUID)

    fun getUser(uniqueId: UUID): U?

    fun getUsers(): List<U>
    fun getUsersAsync(result: (List<U>) -> Unit)

    fun getBasicUsers(useCache: Boolean): List<BasicUser>
    fun getBasicUsersAsync(result: (List<BasicUser>) -> Unit)

    fun addBasicUser(user: BasicUser)
    fun hasBasicUser(uniqueId: UUID, useCache: Boolean): Boolean
    fun updateBasicUser(user: BasicUser)
    fun removeBasicUser(uniqueId: UUID)

    fun isInQueue(uniqueId: UUID, result: (Boolean) -> Unit)
    fun addToQueue(uniqueId: UUID, vanished: Boolean)
    fun getFromQueue(uniqueId: UUID, result: (Boolean) -> Unit)
    fun removeFromQueue(uniqueId: UUID)

    fun purge()
    fun purgeCache()
    fun purgeBasic()
    fun purgeBasic(serverId: String)

    fun updateCacheAsync()
    fun updateBasicCacheAsync()

}