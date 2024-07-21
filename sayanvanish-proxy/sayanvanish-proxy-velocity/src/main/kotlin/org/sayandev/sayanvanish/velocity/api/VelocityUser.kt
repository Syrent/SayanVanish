package org.sayandev.sayanvanish.velocity.api

import com.velocitypowered.api.proxy.Player
import net.kyori.adventure.text.Component
import org.sayandev.sayanvanish.api.User
import org.sayandev.sayanvanish.api.VanishOptions
import org.sayandev.sayanvanish.proxy.config.settings
import org.sayandev.sayanvanish.velocity.event.VelocityUserUnVanishEvent
import org.sayandev.sayanvanish.velocity.event.VelocityUserVanishEvent
import org.sayandev.stickynote.lib.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.sayandev.stickynote.velocity.StickyNote
import org.sayandev.stickynote.velocity.server
import org.sayandev.stickynote.velocity.utils.AdventureUtils.component
import java.util.UUID


open class VelocityUser(
    override val uniqueId: UUID,
    override var username: String
) : User {

    override var serverId = settings.general.serverId
    override var currentOptions = VanishOptions.defaultOptions()
    override var isVanished = false
    override var isOnline: Boolean = false
    override var vanishLevel: Int = 1

    fun stateText(isVanished: Boolean = this.isVanished) = if (isVanished) "<green>ON</green>" else "<red>OFF</red>"

    fun player(): Player? = StickyNote.getPlayer(uniqueId)

    override fun vanish(options: VanishOptions) {
        server.eventManager.fire<VelocityUserVanishEvent>(VelocityUserVanishEvent(this, options)).whenComplete { event, error ->
            error?.printStackTrace()

            val options = event.options
            currentOptions = options

            database.addToQueue(uniqueId, true)
            super.vanish(options)
        }
    }

    override fun unVanish(options: VanishOptions) {
        server.eventManager.fire<VelocityUserUnVanishEvent>(VelocityUserUnVanishEvent(this, options)).whenComplete { event, error ->
            error?.printStackTrace()

            val options = event.options
            currentOptions = options

            database.addToQueue(uniqueId, false)
            super.unVanish(options)
        }
    }

    override fun hasPermission(permission: String): Boolean {
        return player()?.hasPermission(permission) == true
    }

    override fun sendComponent(content: String, vararg placeholder: TagResolver) {
        player()?.sendMessage(content.component())
    }

    override fun sendActionbar(content: String, vararg placeholder: TagResolver) {
        player()?.sendActionBar(content.component())
    }

    companion object {
        @JvmStatic
        fun fromUser(user: User): VelocityUser {
            return VelocityUser(user.uniqueId, user.username).apply {
                this.isOnline = user.isOnline
                this.isVanished = user.isVanished
                this.vanishLevel = user.vanishLevel
            }
        }
    }

}