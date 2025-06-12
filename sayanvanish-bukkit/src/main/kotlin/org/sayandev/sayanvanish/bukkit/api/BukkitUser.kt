package org.sayandev.sayanvanish.bukkit.api

import org.bukkit.entity.Player
import org.sayandev.sayanvanish.api.User
import org.sayandev.sayanvanish.bukkit.config.settings

class BukkitUser {
    companion object {
        fun Player.generateUser(): User {
            return User.of(
                this.uniqueId,
                this.name,
                this.isOnline,
                settings.serverId()
            )
        }
    }
}