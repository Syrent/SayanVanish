package org.sayandev.sayanvanish.bungeecord.api

import net.md_5.bungee.api.connection.ProxiedPlayer
import org.sayandev.sayanvanish.api.SayanVanishAPI
import org.sayandev.sayanvanish.api.User
import org.sayandev.stickynote.bungeecord.launch

class BungeeUser {
    companion object {
        fun ProxiedPlayer.generateUser(): User {
            return User.of(
                this.uniqueId,
                this.name,
                this.isConnected,
                this.server?.info?.name
            )
        }

        fun ProxiedPlayer.generateAndSaveUser(): User {
            val user = this.generateUser()
            launch {
                SayanVanishAPI.get().getDatabase().addUser(user)
            }
            return user
        }
    }
}