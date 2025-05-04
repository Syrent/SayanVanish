package org.sayandev.sayanvanish.velocity.api

import com.velocitypowered.api.proxy.Player
import org.sayandev.sayanvanish.api.SayanVanishAPI
import java.util.UUID

val database = SayanVanishVelocityAPI.getInstance().database

class SayanVanishVelocityAPI() : SayanVanishAPI<VelocityVanishUser>(VelocityVanishUser::class.java) {
    companion object {
        private val defaultInstance = SayanVanishVelocityAPI()

        @JvmStatic
        fun getInstance(): SayanVanishAPI<VelocityVanishUser> {
            return defaultInstance
        }

        @JvmStatic
        public fun UUID.user(): VelocityVanishUser? {
            return getInstance().getUser(this)
        }

        @JvmStatic
        public fun Player.user(): VelocityVanishUser? {
            return getInstance().getUser(this.uniqueId)
        }

        @JvmStatic
        fun Player.getOrCreateUser(): VelocityVanishUser {
            return getInstance().getUser(this.uniqueId) ?: VelocityVanishUser(this.uniqueId, this.username ?: "N/A")
        }

        @JvmStatic
        fun Player.getOrAddUser(): VelocityVanishUser {
            return getInstance().getUser(this.uniqueId) ?: let {
                val newUser = VelocityVanishUser(this.uniqueId, this.username ?: "N/A")
                getInstance().database.addVanishUser(newUser)
                newUser
            }
        }
    }
}