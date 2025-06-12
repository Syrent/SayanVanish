package org.sayandev.sayanvanish.velocity.api

import com.velocitypowered.api.proxy.Player
import org.sayandev.sayanvanish.api.User
import kotlin.jvm.optionals.getOrNull

class VelocityUser {
    companion object {
        fun Player.generateUser(): User {
            return User.of(
                this.uniqueId,
                this.username,
                true,
                this.currentServer.getOrNull()?.serverInfo?.name
            )
        }
    }
}