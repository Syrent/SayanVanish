package org.sayandev.sayanvanish.bukkit.api

import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.sayandev.sayanvanish.api.SayanVanishAPI
import org.sayandev.sayanvanish.api.database.databaseConfig
import org.sayandev.sayanvanish.bukkit.config.settings
import java.util.*

class SayanVanishBukkitAPI : SayanVanishAPI<BukkitUser>(BukkitUser::class.java) {

    fun canSee(player: Player?, otherPlayer: Player): Boolean {
        val vanishLevel = player?.user()?.vanishLevel ?: -1
        return vanishLevel >= (otherPlayer.user()?.vanishLevel ?: -1)
    }

    companion object {
        private val defaultInstance = SayanVanishBukkitAPI()

        @JvmStatic
        fun getInstance(): SayanVanishAPI<BukkitUser> {
            return defaultInstance
        }

        @JvmStatic
        fun UUID.bukkitUser(): BukkitUser? {
            return getInstance().getUser(this)
        }

        @JvmStatic
        fun OfflinePlayer.user(): BukkitUser? {
            return getInstance().database.getUser(this.uniqueId)
        }

        @JvmStatic
        fun OfflinePlayer.getOrCreateUser(): BukkitUser {
            return getInstance().getUser(this.uniqueId) ?: BukkitUser(this.uniqueId, this.name ?: "N/A")
        }

        @JvmStatic
        fun OfflinePlayer.getOrAddUser(): BukkitUser {
            return getInstance().getUser(this.uniqueId) ?: let {
                val newUser = BukkitUser(this.uniqueId, this.name ?: "N/A")
                getInstance().database.addUser(newUser)
                newUser
            }
        }
    }
}