package org.sayandev.sayanvanish.bukkit.api

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.sayandev.sayanvanish.api.User
import org.sayandev.stickynote.bukkit.utils.AdventureUtils
import org.sayandev.stickynote.bukkit.warn
import java.util.*

class BukkitUser(
    override val uniqueId: UUID,
    override var username: String,
    override var isOnline: Boolean,
    override var serverId: String
) : User {

    fun player() = Bukkit.getPlayer(uniqueId)

    fun audience() = player()?.let { AdventureUtils.senderAudience(it) }

    override fun hasPermission(permission: String): Boolean {
        return player()?.hasPermission(Permission(permission, PermissionDefault.FALSE)) == true
    }

    override fun sendMessage(content: Component) {
        audience()?.sendMessage(content)
    }

    override fun sendActionbar(content: Component) {
        audience()?.sendActionBar(content)
    }

    companion object {
        @JvmSynthetic
        fun User.bukkitAdapt(): BukkitUser {
            return BukkitPlatformAdapter.adapt(this)
        }
    }
}