package org.sayandev.sayanvanish.api.cache.caches

import org.sayandev.sayanvanish.api.User
import org.sayandev.sayanvanish.api.cache.Cache
import org.sayandev.sayanvanish.api.cache.ICache
import java.util.UUID

class UserCache: IUserCache, ICache<UUID, User> by Cache("users") {
    override fun getUser(uniqueId: UUID): User? {
        return this[uniqueId]
    }

    override fun getOnline(): List<User> {
        return this.values.filter { it.isOnline }
    }

    override fun getByServer(serverId: String): List<User> {
        return this.values.filter { it.serverId == serverId }
    }

    override fun getCountByServer(serverId: String): Int {
        return this.getByServer(serverId).size
    }

    override fun getOnlineCountByServer(serverId: String): Int {
        return this.getByServer(serverId).count { it.isOnline }
    }

    override fun getOnlineCount(): Int {
        return this.values.count { it.isOnline }
    }

    override fun getCount(): Int {
        return this.values.size
    }

    override fun getOnlineUsersByServerId(serverId: String): List<User> {
        return this.getByServer(serverId).filter { it.isOnline }
    }

    override fun getOnlineCountByServerId(serverId: String): Int {
        return this.getOnlineUsersByServerId(serverId).size
    }
}