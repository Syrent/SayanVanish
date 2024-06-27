package org.sayandev.sayanvanish.bukkit.api

import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.sayandev.sayanvanish.api.SayanVanishAPI
import org.sayandev.sayanvanish.api.database.databaseConfig
import org.sayandev.sayanvanish.bukkit.config.settings
import java.util.*

class SayanVanishBukkitAPI : SayanVanishAPI<BukkitUser>(BukkitUser::class.java) {

    fun canSee(player: Player, otherPlayer: Player): Boolean {
        return player.user()?.canSee(otherPlayer.user() ?: return false) ?: false
    }

    companion object {
        private val defaultInstance = SayanVanishBukkitAPI()

        fun getInstance(): SayanVanishAPI<BukkitUser> {
            return defaultInstance
        }

        fun UUID.bukkitUser(): BukkitUser? {
            return getInstance().getUser(this)
        }

        fun OfflinePlayer.user(): BukkitUser? {
            return getInstance().database.getUser(this.uniqueId)
        }

        fun OfflinePlayer.getOrCreateUser(): BukkitUser {
            return getInstance().getUser(this.uniqueId) ?: BukkitUser(this.uniqueId, this.name ?: "N/A")
        }

        fun OfflinePlayer.getOrAddUser(): BukkitUser {
            return getInstance().getUser(this.uniqueId) ?: let {
                val newUser = BukkitUser(this.uniqueId, this.name ?: "N/A")
                getInstance().database.addUser(newUser)
                newUser
            }
        }
    }
}