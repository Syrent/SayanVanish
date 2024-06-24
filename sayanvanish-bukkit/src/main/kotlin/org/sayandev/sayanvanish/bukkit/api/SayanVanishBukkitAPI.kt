package org.sayandev.sayanvanish.bukkit.api

import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.sayandev.sayanvanish.api.SayanVanishAPI
import org.sayandev.sayanvanish.api.database.databaseConfig
import java.util.*

val database = SayanVanishBukkitAPI.getInstance().database

class SayanVanishBukkitAPI : SayanVanishAPI<BukkitUser>(BukkitUser::class) {

    fun canSee(player: Player, otherPlayer: Player): Boolean {
        return player.user()?.canSee(otherPlayer.user() ?: return false) ?: false
    }

    companion object {
        private val defaultInstance = SayanVanishBukkitAPI()

        fun getInstance(): SayanVanishAPI<BukkitUser> {
            return defaultInstance
        }

        fun UUID.user(useCache: Boolean = databaseConfig.useCacheWhenAvailable): BukkitUser? {
            return getInstance().getUser(this, useCache)
        }

        fun OfflinePlayer.user(useCache: Boolean = databaseConfig.useCacheWhenAvailable): BukkitUser? {
            /*val onlinePlayer = this.player
            if (onlinePlayer != null) {
                if (!onlinePlayer.hasPermission(Permission.VANISH.permission())) {
                    return null
                }
            }*/
            return getInstance().getUser(this.uniqueId, useCache)
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