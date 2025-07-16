package org.sayandev.sayanvanish.bukkit.api

import kotlinx.coroutines.Deferred
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.sayandev.sayanvanish.api.User
import org.sayandev.sayanvanish.api.VanishAPI
import org.sayandev.sayanvanish.api.VanishUser
import org.sayandev.sayanvanish.bukkit.config.SettingsConfig
import java.util.*

class SayanVanishBukkitAPI {
    companion object {
        suspend fun UUID.user(): Deferred<User?>  {
            return VanishAPI.get().getDatabase().getUser(this)
        }

        suspend fun UUID.getOrCreateUser(): User  {
            val offlinePlayer = Bukkit.getOfflinePlayer(this)
            val player = offlinePlayer.player
            return VanishAPI.get().getDatabase().getUser(this).await() ?: User.of(
                this,
                offlinePlayer.name ?: this.toString(),
                player != null,
                SettingsConfig.get().serverId()
            )
        }

        suspend fun UUID.getOrAddUser(): User {
            val user = this.getOrCreateUser()
            VanishAPI.get().getDatabase().saveUser(user).await()
            return user
        }

        fun UUID.cachedUser(): User? {
            return VanishAPI.get().getCacheService().getUsers().getUser(this)
        }

        fun UUID.getCachedOrCreateUser(): User {
            return VanishAPI.get().getCacheService().getUsers().getUser(this) ?: User.of(
                this,
                Bukkit.getOfflinePlayer(this).name ?: "N/A",
                false,
                SettingsConfig.get().serverId()
            )
        }

        suspend fun UUID.vanishUser(): Deferred<VanishUser?> {
            return VanishAPI.get().getDatabase().getVanishUser(this)
        }

        suspend fun UUID.getOrCreateVanishUser(): VanishUser {
            return VanishAPI.get().getDatabase().getVanishUser(this).await() ?: VanishUser.of(
                this,
                Bukkit.getOfflinePlayer(this).name ?: "N/A",
            )
        }

        suspend fun UUID.getOrAddVanishUser(): VanishUser {
            val vanishUser = this.getOrCreateVanishUser()
            if (!VanishAPI.get().getCacheService().getVanishUsers().hasVanishUser(this)) {
                VanishAPI.get().getDatabase().saveVanishUser(vanishUser).await()
            }
            return vanishUser
        }

        fun UUID.cachedVanishUser(): VanishUser? {
            return VanishAPI.get().getCacheService().getVanishUsers().getVanishUser(this)
        }

        fun UUID.getCachedOrCreateVanishUser(): VanishUser {
            return VanishAPI.get().getCacheService().getVanishUsers().getVanishUser(this) ?: VanishUser.of(
                this,
                Bukkit.getOfflinePlayer(this).name ?: "N/A",
            )
        }

        suspend fun OfflinePlayer.user(): Deferred<User?> {
            return this.uniqueId.user()
        }

        suspend fun OfflinePlayer.getOrCreateUser(): User {
            return this.uniqueId.getOrCreateUser()
        }

        suspend fun OfflinePlayer.getOrAddUser(): User {
            return this.uniqueId.getOrAddUser()
        }

        fun OfflinePlayer.cachedUser(): User? {
            return this.uniqueId.cachedUser()
        }

        fun OfflinePlayer.getCachedOrCreateUser(): User {
            return this.uniqueId.getCachedOrCreateUser()
        }

        suspend fun OfflinePlayer.vanishUser(): Deferred<VanishUser?> {
            return this.uniqueId.vanishUser()
        }

        suspend fun OfflinePlayer.getOrCreateVanishUser(): VanishUser {
            return this.uniqueId.getOrCreateVanishUser()
        }

        suspend fun OfflinePlayer.getOrAddVanishUser(): VanishUser {
            return this.uniqueId.getOrAddVanishUser()
        }

        fun OfflinePlayer.cachedVanishUser(): VanishUser? {
            return this.uniqueId.cachedVanishUser()
        }

        fun OfflinePlayer.getCachedOrCreateVanishUser(): VanishUser {
            return this.uniqueId.getCachedOrCreateVanishUser()
        }
    }
}