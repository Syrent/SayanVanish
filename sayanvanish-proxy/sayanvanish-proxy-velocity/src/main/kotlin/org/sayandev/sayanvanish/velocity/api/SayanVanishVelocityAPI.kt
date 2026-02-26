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
