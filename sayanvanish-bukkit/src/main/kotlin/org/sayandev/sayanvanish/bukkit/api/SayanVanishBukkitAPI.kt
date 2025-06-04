package org.sayandev.sayanvanish.bukkit.api

import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.util.*

class SayanVanishBukkitAPI : SayanVanishAPI<BukkitVanishUser>(BukkitVanishUser::class.java) {

    fun canSee(player: Player?, otherPlayer: Player): Boolean {
        val vanishLevel = player?.getOrCreateUser()?.vanishLevel ?: -1
        return vanishLevel >= (otherPlayer.user()?.vanishLevel ?: -1)
    }

    companion object {
        private val defaultInstance = SayanVanishBukkitAPI()

        @JvmStatic
        fun getInstance(): SayanVanishAPI<BukkitVanishUser> {
            return defaultInstance
        }

        @JvmStatic
        fun UUID.bukkitUser(): BukkitVanishUser? {
            return getInstance().getUser(this)
        }

        @JvmStatic
        fun OfflinePlayer.user(): BukkitVanishUser? {
            return getInstance().database.getVanishUser(this.uniqueId)
        }

        @JvmStatic
        fun OfflinePlayer.getOrCreateUser(): BukkitVanishUser {
            return getInstance().getUser(this.uniqueId) ?: BukkitVanishUser(this.uniqueId, this.name ?: "N/A")
        }

        @JvmStatic
        fun OfflinePlayer.getOrAddUser(): BukkitVanishUser {
            return getInstance().getUser(this.uniqueId) ?: let {
                val newUser = BukkitVanishUser(this.uniqueId, this.name ?: "N/A")
                getInstance().database.addVanishUser(newUser)
                newUser
            }
        }
    }
}