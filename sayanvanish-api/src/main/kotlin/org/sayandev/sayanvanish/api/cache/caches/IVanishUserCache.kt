package org.sayandev.sayanvanish.api.cache.caches

import org.sayandev.sayanvanish.api.VanishUser
import org.sayandev.sayanvanish.api.cache.ICache
import java.util.*

interface IVanishUserCache : ICache<UUID, VanishUser> {
    fun getVanishUser(uniqueId: UUID): VanishUser?
    fun hasVanishUser(uniqueId: UUID): Boolean
    fun getOnline(): List<VanishUser>
    fun getByServer(serverId: String): List<VanishUser>
    fun getCountByServer(serverId: String): Int
    fun getOnlineCountByServer(serverId: String): Int
    fun getOnlineCount(): Int
    fun getCount(): Int
    fun getOnlineUsersByServerId(serverId: String): List<VanishUser>
    fun getOnlineCountByServerId(serverId: String): Int
    fun getVanished(): List<VanishUser>
    fun getOnlineVanished(): List<VanishUser>
    fun getVanishedCount(): Int
    fun getOnlineVanishedCount(): Int
}