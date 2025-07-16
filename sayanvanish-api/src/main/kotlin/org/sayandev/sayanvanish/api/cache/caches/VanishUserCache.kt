package org.sayandev.sayanvanish.api.cache.caches

import org.sayandev.sayanvanish.api.VanishUser
import org.sayandev.sayanvanish.api.cache.Cache
import org.sayandev.sayanvanish.api.cache.ICache
import java.util.*

class VanishUserCache: IVanishUserCache, ICache<UUID, VanishUser> by Cache("vanish_user") {
    override fun getVanishUser(uniqueId: UUID): VanishUser? {
        return this[uniqueId]
    }

    override fun hasVanishUser(uniqueId: UUID): Boolean {
        return this.containsKey(uniqueId)
    }

    override fun getOnline(): List<VanishUser> {
        return this.values.filter { it.isOnline }
    }

    override fun getByServer(serverId: String): List<VanishUser> {
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

    override fun getOnlineUsersByServerId(serverId: String): List<VanishUser> {
        return this.getByServer(serverId).filter { it.isOnline }
    }

    override fun getOnlineCountByServerId(serverId: String): Int {
        return this.getOnlineUsersByServerId(serverId).size
    }

    override fun getVanished(): List<VanishUser> {
        return this.values.filter { it.isVanished }
    }

    override fun getOnlineVanished(): List<VanishUser> {
        return this.getVanished().filter { it.isOnline }
    }

    override fun getVanishedCount(): Int {
        return this.getVanished().size
    }

    override fun getOnlineVanishedCount(): Int {
        return this.getOnlineVanished().size
    }
}