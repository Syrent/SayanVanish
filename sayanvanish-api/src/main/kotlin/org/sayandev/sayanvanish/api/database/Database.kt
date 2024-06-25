package org.sayandev.sayanvanish.api.database

import org.sayandev.sayanvanish.api.BasicUser
import org.sayandev.sayanvanish.api.User
import java.util.*
import java.util.function.Consumer
import kotlin.reflect.KClass

interface Database<U: User> {

    fun initialize()

    fun connect()

    fun disconnect()

    fun addUser(user: U)
    fun addUserAsync(user: U, result: () -> Unit)

    fun hasUser(uniqueId: UUID): Boolean
    fun hasUserAsync(uniqueId: UUID, result: (Boolean) -> Unit)

    fun updateUser(user: U)
    fun updateUserAsync(user: U, result: () -> Unit)

    fun removeUser(uniqueId: UUID)
    fun removeUserAsync(uniqueId: UUID, result: () -> Unit)

    fun getUser(uniqueId: UUID, type: KClass<out User>): U?
    fun getUserAsync(uniqueId: UUID, type: KClass<out User>, result: (U?) -> Unit)

    fun getUser(uniqueId: UUID): U?
    fun getUserAsync(uniqueId: UUID, result: (U?) -> Unit)

    fun getUsers(type: KClass<out User>): List<U>
    fun getUsersAsync(type: KClass<out User>, result: (List<U>) -> Unit)

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
    fun updateBasicCache()

}