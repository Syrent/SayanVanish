package org.sayandev.sayanvanish.velocity.api

import com.velocitypowered.api.proxy.Player
import org.sayandev.sayanvanish.api.SayanVanishAPI
import org.sayandev.sayanvanish.api.database.databaseConfig
import java.util.UUID

val database = SayanVanishVelocityAPI.getInstance().database

class SayanVanishVelocityAPI() : SayanVanishAPI<VelocityUser>(VelocityUser::class.java) {
    companion object {
        private val defaultInstance = SayanVanishVelocityAPI()

        @JvmStatic
        fun getInstance(): SayanVanishAPI<VelocityUser> {
            return defaultInstance
        }

        @JvmStatic
        public fun UUID.user(): VelocityUser? {
            return getInstance().getUser(this)
        }

        @JvmStatic
        public fun Player.user(): VelocityUser? {
            return getInstance().getUser(this.uniqueId)
        }

        @JvmStatic
        fun Player.getOrCreateUser(): VelocityUser {
            return getInstance().getUser(this.uniqueId) ?: VelocityUser(this.uniqueId, this.username ?: "N/A")
        }

        @JvmStatic
        fun Player.getOrAddUser(): VelocityUser {
            return getInstance().getUser(this.uniqueId) ?: let {
                val newUser = VelocityUser(this.uniqueId, this.username ?: "N/A")
                getInstance().database.addUser(newUser)
                newUser
            }
        }
    }
}