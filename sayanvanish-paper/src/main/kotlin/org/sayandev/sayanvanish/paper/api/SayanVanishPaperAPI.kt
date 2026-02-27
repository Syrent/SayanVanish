/*
 * This file is part of SayanVanish, licensed under the GNU General Public License v3.0.
 *
 * Copyright (c) 2026 Sayan Development and contributors
 *
 * SayanVanish is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SayanVanish is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.sayandev.sayanvanish.paper.api

import kotlinx.coroutines.Deferred
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.sayandev.sayanvanish.api.User
import org.sayandev.sayanvanish.api.VanishAPI
import org.sayandev.sayanvanish.api.VanishUser
import java.util.*

class SayanVanishPaperAPI {
    companion object {
        @JvmSynthetic
        suspend fun UUID.user(): Deferred<User?>  {
            return VanishAPI.get().getDatabase().getUser(this)
        }

        @JvmSynthetic
        suspend fun UUID.getOrCreateUser(): User  {
            val offlinePlayer = Bukkit.getOfflinePlayer(this)
            val player = offlinePlayer.player
            return VanishAPI.get().getDatabase().getUser(this).await() ?: PaperVanishUser(
                this,
                offlinePlayer.name ?: this.toString()
            )
        }

        @JvmSynthetic
        suspend fun UUID.getOrAddUser(): User {
            val user = this.getOrCreateUser()
            VanishAPI.get().getDatabase().saveUser(user).await()
            return user
        }

        @JvmSynthetic
        fun UUID.cachedUser(): User? {
            return VanishAPI.get().getCacheService().getUsers().getUser(this)
        }

        @JvmSynthetic
        fun UUID.getCachedOrCreateUser(): User {
            return VanishAPI.get().getCacheService().getUsers().getUser(this) ?: PaperVanishUser(
                this,
                Bukkit.getOfflinePlayer(this).name ?: "N/A",
            )
        }

        @JvmSynthetic
        suspend fun UUID.vanishUser(): Deferred<VanishUser?> {
            return VanishAPI.get().getDatabase().getVanishUser(this)
        }

        @JvmSynthetic
        suspend fun UUID.getOrCreateVanishUser(): VanishUser {
            return VanishAPI.get().getDatabase().getVanishUser(this).await() ?: PaperVanishUser(
                this,
                Bukkit.getOfflinePlayer(this).name ?: "N/A",
            )
        }

        @JvmSynthetic
        suspend fun UUID.getOrAddVanishUser(): VanishUser {
            val vanishUser = this.getOrCreateVanishUser()
            if (!VanishAPI.get().getCacheService().getVanishUsers().hasVanishUser(this)) {
                VanishAPI.get().getDatabase().saveVanishUser(vanishUser).await()
            }
            return vanishUser
        }

        @JvmSynthetic
        fun UUID.cachedVanishUser(): VanishUser? {
            return VanishAPI.get().getCacheService().getVanishUsers().getVanishUser(this)
        }

        @JvmSynthetic
        fun UUID.getCachedOrCreateVanishUser(): VanishUser {
            return VanishAPI.get().getCacheService().getVanishUsers().getVanishUser(this) ?: PaperVanishUser(
                this,
                Bukkit.getOfflinePlayer(this).name ?: "N/A",
            )
        }

        @JvmSynthetic
        suspend fun OfflinePlayer.user(): Deferred<User?> {
            return this.uniqueId.user()
        }

        @JvmSynthetic
        suspend fun OfflinePlayer.getOrCreateUser(): User {
            return this.uniqueId.getOrCreateUser()
        }

        @JvmSynthetic
        suspend fun OfflinePlayer.getOrAddUser(): User {
            return this.uniqueId.getOrAddUser()
        }

        @JvmSynthetic
        fun OfflinePlayer.cachedUser(): User? {
            return this.uniqueId.cachedUser()
        }

        @JvmSynthetic
        fun OfflinePlayer.getCachedOrCreateUser(): User {
            return this.uniqueId.getCachedOrCreateUser()
        }

        @JvmSynthetic
        suspend fun OfflinePlayer.vanishUser(): Deferred<VanishUser?> {
            return this.uniqueId.vanishUser()
        }

        @JvmSynthetic
        suspend fun OfflinePlayer.getOrCreateVanishUser(): VanishUser {
            return this.uniqueId.getOrCreateVanishUser()
        }

        @JvmSynthetic
        suspend fun OfflinePlayer.getOrAddVanishUser(): VanishUser {
            return this.uniqueId.getOrAddVanishUser()
        }

        @JvmSynthetic
        fun OfflinePlayer.cachedVanishUser(): VanishUser? {
            return this.uniqueId.cachedVanishUser()
        }

        @JvmSynthetic
        fun OfflinePlayer.getCachedOrCreateVanishUser(): VanishUser {
            return this.uniqueId.getCachedOrCreateVanishUser()
        }
    }
}
