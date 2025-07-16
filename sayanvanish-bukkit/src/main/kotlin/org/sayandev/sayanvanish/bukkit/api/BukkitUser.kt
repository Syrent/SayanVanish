package org.sayandev.sayanvanish.bukkit.api

import org.bukkit.entity.Player
import org.sayandev.sayanvanish.api.User
import org.sayandev.sayanvanish.bukkit.config.SettingsConfig

class BukkitUser {
    companion object {
        fun Player.generateUser(): User {
            return User.of(
                this.uniqueId,
                this.name,
                this.isOnline,
                SettingsConfig.get().serverId()
            )
        }
    }
}