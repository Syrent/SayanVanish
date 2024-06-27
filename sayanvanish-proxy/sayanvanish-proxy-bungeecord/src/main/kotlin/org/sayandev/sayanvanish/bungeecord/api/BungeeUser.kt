package org.sayandev.sayanvanish.bungeecord.api

import net.md_5.bungee.api.connection.ProxiedPlayer
import org.sayandev.sayanvanish.api.User
import org.sayandev.sayanvanish.api.VanishOptions
import org.sayandev.sayanvanish.bungeecord.event.BungeeUserUnVanishEvent
import org.sayandev.sayanvanish.bungeecord.event.BungeeUserVanishEvent
import org.sayandev.sayanvanish.proxy.config.settings
import org.sayandev.stickynote.bungeecord.utils.AdventureUtils.component
import org.sayandev.stickynote.bungeecord.utils.AdventureUtils.sendMessage
import org.sayandev.stickynote.bungeecord.utils.AdventureUtils.sendActionbar
import org.sayandev.stickynote.bungeecord.StickyNote
import org.sayandev.stickynote.bungeecord.plugin
import org.sayandev.stickynote.lib.kyori.adventure.text.Component
import java.util.UUID


open class BungeeUser(
    override val uniqueId: UUID,
    override var username: String
) : User {

    override var serverId = settings.general.serverId
    override var currentOptions = VanishOptions.defaultOptions()
    override var isVanished = false
    override var isOnline: Boolean = false
    override var vanishLevel: Int = 1

    fun stateText(isVanished: Boolean = this.isVanished) = if (isVanished) "<green>ON</green>" else "<red>OFF</red>"

    fun player(): ProxiedPlayer? = StickyNote.getPlayer(uniqueId)

    override fun vanish(options: VanishOptions) {
        val vanishEvent = plugin.proxy.pluginManager.callEvent(BungeeUserVanishEvent(this, options))
        if (vanishEvent.isCancelled) return

        val options = vanishEvent.options
        currentOptions = options

        database.addToQueue(uniqueId, true)
        super.vanish(options)
    }

    override fun unVanish(options: VanishOptions) {
        val vanishEvent = plugin.proxy.pluginManager.callEvent(BungeeUserUnVanishEvent(this, options))
        if (vanishEvent.isCancelled) return

        val options = vanishEvent.options
        currentOptions = options

        database.addToQueue(uniqueId, false)
        super.unVanish(options)
    }

    override fun hasPermission(permission: String): Boolean {
        return player()?.hasPermission(permission) == true
    }

    override fun sendMessage(content: String) {
        player()?.sendMessage(content.component())
    }

    fun sendMessage(content: Component) {
        player()?.sendMessage(content)
    }

    override fun sendActionbar(content: String) {
        player()?.sendActionbar(content.component())
    }

    fun sendActionbar(content: Component) {
        player()?.sendActionbar(content)
    }

    companion object {
        @JvmStatic
        fun fromUser(user: User): BungeeUser {
            return BungeeUser(user.uniqueId, user.username).apply {
                this.isOnline = user.isOnline
                this.isVanished = user.isVanished
                this.vanishLevel = user.vanishLevel
            }
        }
    }

}