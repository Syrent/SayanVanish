package org.sayandev.sayanvanish.bungeecord.api

import net.md_5.bungee.api.connection.ProxiedPlayer
import org.sayandev.sayanvanish.api.SayanVanishAPI
import org.sayandev.sayanvanish.api.database.databaseConfig
import java.util.UUID

val database = SayanVanishBungeeAPI.getInstance().database

class SayanVanishBungeeAPI() : SayanVanishAPI<BungeeUser>(BungeeUser::class.java) {
    companion object {
        private val defaultInstance = SayanVanishBungeeAPI()

        fun getInstance(): SayanVanishAPI<BungeeUser> {
            return defaultInstance
        }

        public fun UUID.user(): BungeeUser? {
            return getInstance().getUser(this)
        }

        public fun ProxiedPlayer.user(): BungeeUser? {
            return getInstance().getUser(this.uniqueId)
        }

        fun ProxiedPlayer.getOrCreateUser(): BungeeUser {
            return getInstance().getUser(this.uniqueId) ?: BungeeUser(this.uniqueId, this.name ?: "N/A")
        }

        fun ProxiedPlayer.getOrAddUser(): BungeeUser {
            return getInstance().getUser(this.uniqueId) ?: let {
                val newUser = BungeeUser(this.uniqueId, this.name ?: "N/A")
                getInstance().database.addUser(newUser)
                newUser
            }
        }
    }
}