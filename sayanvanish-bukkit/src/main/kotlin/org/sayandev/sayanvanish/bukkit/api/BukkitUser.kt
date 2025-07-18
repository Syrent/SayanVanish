package org.sayandev.sayanvanish.bukkit.api

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.sayandev.sayanvanish.api.User
import org.sayandev.sayanvanish.bukkit.config.Settings
import org.sayandev.stickynote.bukkit.utils.AdventureUtils
import java.util.UUID

class BukkitUser(
    override val uniqueId: UUID,
    override var username: String,
    override var isOnline: Boolean,
    override var serverId: String
) : User {

    fun player() = Bukkit.getPlayer(uniqueId)

    fun audience() = player()?.let { AdventureUtils.senderAudience(it) }

    override fun sendMessage(content: Component) {
        audience()?.sendMessage(content)
    }

    override fun sendActionbar(content: Component) {
        audience()?.sendActionBar(content)
    }

    companion object {
        fun Player.generateUser(): User {
            return User.Generic(
                this.uniqueId,
                this.name,
                this.isOnline,
                Settings.get().serverId()
            )
        }
    }
}