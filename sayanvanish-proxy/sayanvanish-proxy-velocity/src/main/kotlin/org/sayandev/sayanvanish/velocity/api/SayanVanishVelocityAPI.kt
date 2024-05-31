package org.sayandev.sayanvanish.velocity.api

import com.velocitypowered.api.proxy.Player
import org.sayandev.sayanvanish.api.SayanVanishAPI
import org.sayandev.sayanvanish.api.database.databaseConfig
import java.util.UUID

val database = SayanVanishVelocityAPI.getInstance().database

class SayanVanishVelocityAPI(useCache: Boolean) : SayanVanishAPI<VelocityUser>(VelocityUser::class, useCache) {
    companion object {
        private val cachedInstance = SayanVanishVelocityAPI(true)
        private val defaultInstance = SayanVanishVelocityAPI(false)

        fun getInstance(useCache: Boolean): SayanVanishAPI<VelocityUser> {
            return if (useCache) cachedInstance else defaultInstance
        }

        fun getInstance(): SayanVanishAPI<VelocityUser> {
            return if (databaseConfig.useCacheWhenAvailable) cachedInstance else defaultInstance
        }

        public fun UUID.user(): VelocityUser? {
            return getInstance().getUser(this)
        }

        public fun Player.user(useCache: Boolean = databaseConfig.useCacheWhenAvailable): VelocityUser? {
            return getInstance(useCache).getUser(this.uniqueId)
        }

        fun Player.getOrCreateUser(): VelocityUser {
            return getInstance().getUser(this.uniqueId) ?: VelocityUser(this.uniqueId, this.username ?: "N/A")
        }

        fun Player.getOrAddUser(): VelocityUser {
            return getInstance().getUser(this.uniqueId) ?: let {
                val newUser = VelocityUser(this.uniqueId, this.username ?: "N/A")
                getInstance().addUser(newUser)
                newUser
            }
        }
    }
}