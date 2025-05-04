package org.sayandev.sayanvanish.velocity.api

import com.velocitypowered.api.proxy.Player
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.sayandev.sayanvanish.api.Permission
import org.sayandev.sayanvanish.api.SayanVanishAPI
import org.sayandev.sayanvanish.api.VanishUser
import org.sayandev.sayanvanish.api.VanishOptions
import org.sayandev.sayanvanish.api.feature.Features
import org.sayandev.sayanvanish.proxy.config.settings
import org.sayandev.sayanvanish.velocity.event.VelocityUserUnVanishEvent
import org.sayandev.sayanvanish.velocity.event.VelocityUserVanishEvent
import org.sayandev.sayanvanish.velocity.feature.features.hook.FeatureLuckPermsHook
import org.sayandev.sayanvanish.velocity.utils.PlayerUtils.sendComponent
import org.sayandev.stickynote.velocity.StickyNote
import org.sayandev.stickynote.velocity.server
import org.sayandev.stickynote.velocity.utils.AdventureUtils.component
import java.util.*
import kotlin.jvm.optionals.getOrNull


open class VelocityVanishUser(
    override val uniqueId: UUID,
    override var username: String
) : VanishUser {

    override var serverId: String
        get() = StickyNote.getPlayer(uniqueId)?.currentServer?.getOrNull()?.serverInfo?.name ?: settings.general.serverId
        set(_) {}
    override var currentOptions = VanishOptions.defaultOptions()
    override var isVanished = false
    override var isOnline: Boolean = SayanVanishAPI.getInstance().database.hasUser(uniqueId, true)
    override var vanishLevel: Int = 0
        get() = player()?.let { player ->
                val luckPermsHook = Features.getFeature<FeatureLuckPermsHook>()
                    if (luckPermsHook.isActive()) {
                        luckPermsHook.getPermissions(uniqueId)
                            .filter { it.startsWith("sayanvanish.level.") }
                            .maxOfOrNull { it.substringAfter("sayanvanish.level.").toIntOrNull() ?: field }
                            ?: if (hasPermission(Permission.VANISH)) 1 else {
                                if (isVanished) 1 else field
                            }
                    } else {
                        field
                    }
            } ?: field

    fun stateText(isVanished: Boolean = this.isVanished) = if (isVanished) "<green>ON</green>" else "<red>OFF</red>"

    fun player(): Player? = StickyNote.getPlayer(uniqueId)

    override fun vanish(options: VanishOptions) {
        server.eventManager.fire(VelocityUserVanishEvent(this, options)).whenComplete { event, error ->
            error?.printStackTrace()

            val options = event.options
            currentOptions = options

            database.addToQueue(uniqueId, true)
            super.vanish(options)
        }
    }

    override fun unVanish(options: VanishOptions) {
        server.eventManager.fire(VelocityUserUnVanishEvent(this, options)).whenComplete { event, error ->
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
        player()?.sendComponent(content, *placeholder)
    }

    override fun sendActionbar(content: String, vararg placeholder: TagResolver) {
        player()?.sendActionBar(content.component(*placeholder))
    }

    companion object {
        @JvmStatic
        fun fromUser(vanishUser: VanishUser): VelocityVanishUser {
            return VelocityVanishUser(vanishUser.uniqueId, vanishUser.username).apply {
                this.isOnline = vanishUser.isOnline
                this.isVanished = vanishUser.isVanished
                this.vanishLevel = vanishUser.vanishLevel
            }
        }
    }

}