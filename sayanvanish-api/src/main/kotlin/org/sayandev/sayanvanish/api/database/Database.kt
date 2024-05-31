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

    fun getUser(uniqueId: UUID, useCache: Boolean, type: KClass<out User>): U?

    fun getUser(uniqueId: UUID, useCache: Boolean): U?

    fun getUsers(useCache: Boolean, type: KClass<out User>): List<U>

    fun getUsers(useCache: Boolean): List<U>

    fun getBasicUsers(useCache: Boolean): List<BasicUser>

    fun updateUser(user: U)
    fun updateBasicUser(user: BasicUser)

    fun addUser(user: U)
    fun addBasicUser(user: BasicUser)

    fun hasUser(uniqueId: UUID, useCache: Boolean): Boolean
    fun hasBasicUser(uniqueId: UUID, useCache: Boolean): Boolean

    fun removeUser(uniqueId: UUID)
    fun removeBasicUser(uniqueId: UUID)

    fun isInQueue(uniqueId: UUID, result: Consumer<Boolean>)
    fun addToQueue(uniqueId: UUID, vanished: Boolean)
    fun getFromQueue(uniqueId: UUID, result: Consumer<Boolean>)
    fun removeFromQueue(uniqueId: UUID)

    fun purgeCache()

    fun purge()
    fun purgeBasic()
    fun purgeBasic(serverId: String)
    fun updateBasicCache()

}