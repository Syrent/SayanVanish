package org.sayandev.sayanvanish.velocity.api

import com.velocitypowered.api.proxy.Player
import org.sayandev.sayanvanish.api.SayanVanishAPI
import org.sayandev.sayanvanish.api.database.databaseConfig
import java.util.UUID

val database = SayanVanishVelocityAPI.getInstance().database

class SayanVanishVelocityAPI() : SayanVanishAPI<VelocityUser>(VelocityUser::class) {
    companion object {
        private val defaultInstance = SayanVanishVelocityAPI()

        fun getInstance(): SayanVanishAPI<VelocityUser> {
            return defaultInstance
        }

        public fun UUID.user(): VelocityUser? {
            return getInstance().getUser(this)
        }

        public fun Player.user(useCache: Boolean = databaseConfig.useCacheWhenAvailable): VelocityUser? {
            return getInstance().getUser(this.uniqueId, useCache)
        }

        fun Player.getOrCreateUser(useCache: Boolean = databaseConfig.useCacheWhenAvailable): VelocityUser {
            return getInstance().getUser(this.uniqueId, useCache) ?: VelocityUser(this.uniqueId, this.username ?: "N/A")
        }

        fun Player.getOrAddUser(useCache: Boolean = databaseConfig.useCacheWhenAvailable): VelocityUser {
            return getInstance().getUser(this.uniqueId, useCache) ?: let {
                val newUser = VelocityUser(this.uniqueId, this.username ?: "N/A")
                getInstance().addUser(newUser)
                newUser
            }
        }
    }
}