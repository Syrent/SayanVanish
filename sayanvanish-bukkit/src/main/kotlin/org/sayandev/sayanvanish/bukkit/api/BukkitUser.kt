package org.sayandev.sayanvanish.bukkit.api

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Bukkit
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.sayandev.sayanvanish.api.Permissions
import org.sayandev.sayanvanish.api.User
import org.sayandev.sayanvanish.bukkit.utils.PermissionUtils.asBukkitPermissionDefault
import org.sayandev.sayanvanish.bukkit.utils.PlayerUtils.sendPrefixComponent
import org.sayandev.stickynote.bukkit.extension.sendComponent
import org.sayandev.stickynote.bukkit.extension.sendComponentActionbar
import org.sayandev.stickynote.bukkit.utils.AdventureUtils
import org.sayandev.stickynote.bukkit.utils.AdventureUtils.component
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

    override fun hasPermission(permission: Permissions): Boolean {
        return player()?.hasPermission(Permission(permission.permission(), permission.default.asBukkitPermissionDefault())) == true
    }

    override fun sendMessage(content: String, vararg placeholders: TagResolver) {
        sendMessage(content.component(*placeholders))
    }

    override fun sendActionbar(content: String, vararg placeholders: TagResolver) {
        sendActionbar(content.component(*placeholders))
    }

    fun sendMessage(content: Component) {
        player()?.sendComponent(content)
    }

    override fun sendMessageWithPrefix(content: String, vararg placeholders: TagResolver) {
        player()?.sendPrefixComponent(content, *placeholders)
    }

    fun sendActionbar(content: Component) {
        player()?.sendComponentActionbar(content)
    }

    companion object {
        @JvmSynthetic
        fun User.bukkitAdapt(): BukkitUser {
            return BukkitPlatformAdapter.adapt(this)
        }
    }
}