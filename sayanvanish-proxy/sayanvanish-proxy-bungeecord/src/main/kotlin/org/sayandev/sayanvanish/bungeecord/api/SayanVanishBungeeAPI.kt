package org.sayandev.sayanvanish.bungeecord.api

import BungeePlatformAdapter
import net.md_5.bungee.api.connection.ProxiedPlayer
import org.sayandev.sayanvanish.api.PlatformAdapter
import org.sayandev.sayanvanish.api.SayanVanishAPI
import org.sayandev.sayanvanish.api.SayanVanishAPI.user
import org.sayandev.sayanvanish.api.VanishAPI
import org.sayandev.sayanvanish.api.VanishUser

object SayanVanishBungeeAPI : VanishAPI by SayanVanishAPI, PlatformAdapter<BungeeVanishUser> by BungeePlatformAdapter {
    @JvmStatic
    fun get(): SayanVanishBungeeAPI {
        return this
    }

    @JvmStatic
    suspend fun ProxiedPlayer.user(): VanishUser? {
        return this.uniqueId.user()
    }

    @JvmStatic
    suspend fun ProxiedPlayer.getOrCreateUser(): VanishUser {
        return getDatabase().getVanishUser(this.uniqueId).await() ?: VanishUser.of(this.uniqueId, this.name ?: "N/A")
    }

    @JvmStatic
    suspend fun ProxiedPlayer.getOrAddUser(): VanishUser {
        return getDatabase().getVanishUser(this.uniqueId).await() ?: let {
            val newUser = BungeeVanishUser(this.uniqueId, this.name ?: "N/A")
            getDatabase().saveVanishUser(newUser).await()
            newUser
        }
    }
}