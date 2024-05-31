package org.sayandev.sayanvanish.bukkit.api

import org.bukkit.OfflinePlayer
import org.sayandev.sayanvanish.api.SayanVanishAPI
import org.sayandev.sayanvanish.api.database.databaseConfig
import java.util.*

val database = SayanVanishBukkitAPI.getInstance().database

class SayanVanishBukkitAPI(useCache: Boolean) : SayanVanishAPI<BukkitUser>(BukkitUser::class, useCache) {
    companion object {
        private val cachedInstance = SayanVanishBukkitAPI(true)
        private val defaultInstance = SayanVanishBukkitAPI(false)

        fun getInstance(useCache: Boolean): SayanVanishAPI<BukkitUser> {
            return if (useCache) cachedInstance else defaultInstance
        }

        fun getInstance(): SayanVanishAPI<BukkitUser> {
            return if (databaseConfig.useCacheWhenAvailable) cachedInstance else defaultInstance
        }

        fun UUID.user(): BukkitUser? {
            return getInstance().getUser(this)
        }

        fun OfflinePlayer.user(useCache: Boolean = databaseConfig.useCacheWhenAvailable): BukkitUser? {
            /*val onlinePlayer = this.player
            if (onlinePlayer != null) {
                if (!onlinePlayer.hasPermission(Permission.VANISH.permission())) {
                    return null
                }
            }*/
            return getInstance(useCache).getUser(this.uniqueId)
        }

        fun OfflinePlayer.getOrCreateUser(): BukkitUser {
            return getInstance().getUser(this.uniqueId) ?: BukkitUser(this.uniqueId, this.name ?: "N/A")
        }

        fun OfflinePlayer.getOrAddUser(): BukkitUser {
            return getInstance().getUser(this.uniqueId) ?: let {
                val newUser = BukkitUser(this.uniqueId, this.name ?: "N/A")
                getInstance().addUser(newUser)
                newUser
            }
        }
    }
}