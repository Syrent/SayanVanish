package org.sayandev.sayanvanish.bungeecord.api

import net.md_5.bungee.api.connection.ProxiedPlayer
import org.sayandev.sayanvanish.api.SayanVanishAPI
import java.util.UUID

val database = SayanVanishBungeeAPI.getInstance().database

class SayanVanishBungeeAPI : SayanVanishAPI<BungeeVanishUser>(BungeeVanishUser::class.java) {
    companion object {
        private val defaultInstance = SayanVanishBungeeAPI()

        @JvmStatic
        fun getInstance(): SayanVanishAPI<BungeeVanishUser> {
            return defaultInstance
        }

        @JvmStatic
        public fun UUID.user(): BungeeVanishUser? {
            return getInstance().getUser(this)
        }

        @JvmStatic
        public fun ProxiedPlayer.user(): BungeeVanishUser? {
            return getInstance().getUser(this.uniqueId)
        }

        @JvmStatic
        fun ProxiedPlayer.getOrCreateUser(): BungeeVanishUser {
            return getInstance().getUser(this.uniqueId) ?: BungeeVanishUser(this.uniqueId, this.name ?: "N/A")
        }

        @JvmStatic
        fun ProxiedPlayer.getOrAddUser(): BungeeVanishUser {
            return getInstance().getUser(this.uniqueId) ?: let {
                val newUser = BungeeVanishUser(this.uniqueId, this.name ?: "N/A")
                getInstance().database.addVanishUser(newUser)
                newUser
            }
        }
    }
}