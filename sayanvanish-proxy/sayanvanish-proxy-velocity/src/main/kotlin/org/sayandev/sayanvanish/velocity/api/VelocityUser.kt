package org.sayandev.sayanvanish.velocity.api

import com.velocitypowered.api.proxy.Player
import net.kyori.adventure.text.Component
import org.sayandev.sayanvanish.api.User
import org.sayandev.sayanvanish.api.VanishOptions
import org.sayandev.sayanvanish.velocity.config.settings
import org.sayandev.stickynote.velocity.StickyNote
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
        database.addToQueue(uniqueId, true)
        super.vanish(options)
    }

    override fun unVanish(options: VanishOptions) {
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
        player()?.sendActionBar(content.component())
    }

    fun sendActionbar(content: Component) {
        player()?.sendActionBar(content)
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