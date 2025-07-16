package org.sayandev.sayanvanish.api.cache.caches

import org.sayandev.sayanvanish.api.User
import org.sayandev.sayanvanish.api.cache.ICache
import java.util.UUID

interface IUserCache : ICache<UUID, User> {
    fun getUser(uniqueId: UUID): User?
    fun getOnline(): List<User>
    fun getByServer(serverId: String): List<User>
    fun getCountByServer(serverId: String): Int
    fun getOnlineCountByServer(serverId: String): Int
    fun getOnlineCount(): Int
    fun getCount(): Int
    fun getOnlineUsersByServerId(serverId: String): List<User>
    fun getOnlineCountByServerId(serverId: String): Int
}