package org.sayandev.sayanvanish.bungeecord.api

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.md_5.bungee.api.connection.ProxiedPlayer
import org.sayandev.sayanvanish.api.SayanVanishAPI
import org.sayandev.sayanvanish.api.VanishUser
import org.sayandev.sayanvanish.api.VanishOptions
import org.sayandev.sayanvanish.bungeecord.event.BungeeUserUnVanishEvent
import org.sayandev.sayanvanish.bungeecord.event.BungeeUserVanishEvent
import org.sayandev.sayanvanish.bungeecord.utils.PlayerUtils.sendComponent
import org.sayandev.sayanvanish.proxy.config.settings
import org.sayandev.stickynote.bungeecord.utils.AdventureUtils.component
import org.sayandev.stickynote.bungeecord.utils.AdventureUtils.sendActionbar
import org.sayandev.stickynote.bungeecord.StickyNote
import org.sayandev.stickynote.bungeecord.plugin
import java.util.UUID


open class BungeeVanishUser(
    override val uniqueId: UUID,
    override var username: String
) : VanishUser {

    override var serverId = settings.general.serverId
    override var currentOptions = VanishOptions.defaultOptions()
    override var isVanished = false
    override var isOnline: Boolean = SayanVanishAPI.getInstance().database.hasUser(uniqueId, true)
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

    override fun sendComponent(content: String, vararg placeholder: TagResolver) {
        player()?.sendComponent(content.component())
    }

    override fun sendActionbar(content: String, vararg placeholder: TagResolver) {
        player()?.sendActionbar(content.component())
    }

    companion object {
        @JvmStatic
        fun fromUser(vanishUser: VanishUser): BungeeVanishUser {
            return BungeeVanishUser(vanishUser.uniqueId, vanishUser.username).apply {
                this.isOnline = vanishUser.isOnline
                this.isVanished = vanishUser.isVanished
                this.vanishLevel = vanishUser.vanishLevel
            }
        }
    }

}