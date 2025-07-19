package org.sayandev.sayanvanish.bukkit.api

import org.bukkit.Bukkit
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.sayandev.sayanvanish.api.User
import org.sayandev.sayanventure.adventure.audience.Audience
import org.sayandev.sayanventure.adventure.text.Component
import org.sayandev.stickynote.bukkit.extension.sendComponent
import org.sayandev.stickynote.bukkit.extension.sendComponentActionbar
import org.sayandev.stickynote.bukkit.utils.AdventureUtils
import java.util.*

class BukkitUser(
    override val uniqueId: UUID,
    override var username: String,
    override var isOnline: Boolean,
    override var serverId: String
) : User {

    fun player() = Bukkit.getPlayer(uniqueId)

    fun audience(): Audience? = player()?.let { AdventureUtils.senderAudience(it) }

    override fun hasPermission(permission: String): Boolean {
        return player()?.hasPermission(Permission(permission, PermissionDefault.FALSE)) == true
    }

    override fun sendMessage(content: Component) {
        player()?.sendComponent(content)
    }

    override fun sendActionbar(content: Component) {
        player()?.sendComponentActionbar(content)
    }

    companion object {
        @JvmSynthetic
        fun User.bukkitAdapt(): BukkitUser {
            return BukkitPlatformAdapter.adapt(this)
        }
    }
}