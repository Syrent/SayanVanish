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
package org.sayandev.sayanvanish.velocity.api

import com.velocitypowered.api.proxy.Player
import kotlinx.coroutines.Deferred
import org.sayandev.sayanvanish.api.Platform
import org.sayandev.sayanvanish.api.User
import org.sayandev.sayanvanish.api.VanishAPI
import org.sayandev.sayanvanish.api.VanishUser
import org.sayandev.stickynote.velocity.StickyNote
import java.util.*
import kotlin.jvm.optionals.getOrNull

class SayanVanishVelocityAPI {
    companion object {
        @JvmSynthetic
        suspend fun UUID.user(): Deferred<User?>  {
            return VanishAPI.get().getDatabase().getUser(this)
        }

        @JvmSynthetic
        fun UUID.cachedUser(): User? {
            return VanishAPI.get().getCacheService().getUsers().getUser(this)
        }

        @JvmSynthetic
        suspend fun UUID.vanishUser(): Deferred<VanishUser?> {
            return VanishAPI.get().getDatabase().getVanishUser(this)
        }

        @JvmSynthetic
        fun UUID.cachedVanishUser(): VanishUser? {
            return VanishAPI.get().getCacheService().getVanishUsers().getVanishUser(this)
        }

        @JvmSynthetic
        suspend fun Player.user(): Deferred<User?> {
            return this.uniqueId.user()
        }

        @JvmSynthetic
        suspend fun Player.getOrCreateUser(): User {
            return VanishAPI.get().getDatabase().getUser(this.uniqueId).await() ?: VelocityVanishUser(
                this.uniqueId,
                this.username,
            )
        }

        @JvmSynthetic
        suspend fun Player.getOrAddUser(): User {
            val user = this.getOrCreateUser()
            VanishAPI.get().getDatabase().saveUser(user).await()
            return user
        }

        @JvmSynthetic
        fun Player.cachedUser(): User? {
            return VanishAPI.get().getCacheService().getUsers().getUser(this.uniqueId)
        }

        @JvmSynthetic
        fun Player.getCachedOrCreateUser(): User {
            return VanishAPI.get().getCacheService().getUsers().getUser(this.uniqueId) ?: VelocityUser(
                this.uniqueId,
                this.username,
                false,
                Platform.get().serverId
            )
        }

        @JvmSynthetic
        suspend fun Player.vanishUser(): Deferred<VanishUser?> {
            return this.uniqueId.vanishUser()
        }

        @JvmSynthetic
        suspend fun Player.getOrCreateVanishUser(): VanishUser {
            return VanishAPI.get().getDatabase().getVanishUser(this.uniqueId).await() ?: VelocityVanishUser(
                this.uniqueId,
                this.username,
            )
        }

        @JvmSynthetic
        suspend fun Player.getOrAddVanishUser(): VanishUser {
            val vanishUser = this.getOrCreateVanishUser()
            if (!VanishAPI.get().getCacheService().getVanishUsers().hasVanishUser(this.uniqueId)) {
                VanishAPI.get().getDatabase().saveVanishUser(vanishUser).await()
            }
            return vanishUser
        }

        @JvmSynthetic
        fun Player.cachedVanishUser(): VanishUser? {
            return this.uniqueId.cachedVanishUser()
        }

        @JvmSynthetic
        fun Player.getCachedOrCreateVanishUser(): VanishUser {
            return VanishAPI.get().getCacheService().getVanishUsers().getVanishUser(this.uniqueId) ?: VelocityVanishUser(
                this.uniqueId,
                this.username,
            )
        }
    }
}
