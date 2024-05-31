package org.sayandev.sayanvanish.bungeecord.api

import net.md_5.bungee.api.connection.ProxiedPlayer
import org.sayandev.sayanvanish.api.SayanVanishAPI
import org.sayandev.sayanvanish.api.database.databaseConfig
import java.util.UUID

val database = SayanVanishBungeeAPI.getInstance().database

class SayanVanishBungeeAPI(useCache: Boolean) : SayanVanishAPI<BungeeUser>(BungeeUser::class, useCache) {
    companion object {
        private val cachedInstance = SayanVanishBungeeAPI(true)
        private val defaultInstance = SayanVanishBungeeAPI(false)

        fun getInstance(useCache: Boolean): SayanVanishAPI<BungeeUser> {
            return if (useCache) cachedInstance else defaultInstance
        }

        fun getInstance(): SayanVanishAPI<BungeeUser> {
            return if (databaseConfig.useCacheWhenAvailable) cachedInstance else defaultInstance
        }

        public fun UUID.user(): BungeeUser? {
            return getInstance().getUser(this)
        }

        public fun ProxiedPlayer.user(useCache: Boolean = databaseConfig.useCacheWhenAvailable): BungeeUser? {
            return getInstance(useCache).getUser(this.uniqueId)
        }

        fun ProxiedPlayer.getOrCreateUser(): BungeeUser {
            return getInstance().getUser(this.uniqueId) ?: BungeeUser(this.uniqueId, this.name ?: "N/A")
        }

        fun ProxiedPlayer.getOrAddUser(): BungeeUser {
            return getInstance().getUser(this.uniqueId) ?: let {
                val newUser = BungeeUser(this.uniqueId, this.name ?: "N/A")
                getInstance().addUser(newUser)
                newUser
            }
        }
    }
}