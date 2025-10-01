package org.sayandev.sayanvanish.velocity.api

import com.velocitypowered.api.proxy.Player
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.sayandev.sayanvanish.api.Permission
import org.sayandev.sayanvanish.api.VanishAPI
import org.sayandev.sayanvanish.api.VanishOptions
import org.sayandev.sayanvanish.api.VanishUser
import org.sayandev.sayanvanish.api.feature.Features
import org.sayandev.sayanvanish.proxy.config.Settings
import org.sayandev.sayanvanish.velocity.VelocityPlatformAdapter
import org.sayandev.sayanvanish.velocity.feature.features.hook.FeatureHookLuckPerms
import org.sayandev.stickynote.velocity.StickyNote
import org.sayandev.stickynote.velocity.utils.AdventureUtils.component
import java.util.*
import kotlin.jvm.optionals.getOrNull

open class VelocityVanishUser(
    override val uniqueId: UUID,
    override var username: String
) : VanishUser {

    override var serverId: String
        get() = player()?.currentServer?.getOrNull()?.serverInfo?.name ?: Settings.get().general.serverId
        set(_) {}
    override var currentOptions = VanishOptions.defaultOptions()
    override var isVanished = false
    override var isOnline: Boolean = player() != null
    override var vanishLevel: Int = 0
        get() = player()?.let { player ->
                val luckPermsHook = Features.getFeature<FeatureHookLuckPerms>()
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

    override fun stateText(isVanished: Boolean) = if (isVanished) "<green>ON</green>" else "<red>OFF</red>"

    fun player(): Player? = StickyNote.getPlayer(uniqueId)

    override fun disappear(options: VanishOptions) {
        // TODO: create event. and make sure to apply the options from the event
        super.disappear(options)
    }

    override fun appear(options: VanishOptions) {
        // TODO: create event. and make sure to apply the options from the event
        super.appear(options)
    }

    override fun hasPermission(permission: String): Boolean {
        return player()?.hasPermission(permission) == true
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

        @JvmStatic
        suspend fun Player.getVanishUser(): VelocityVanishUser? {
            val player = this
            return VanishAPI.get().getDatabase().getVanishUser(player.uniqueId).await()?.let { VelocityPlatformAdapter.adapt(it) }
        }

        @JvmStatic
        fun Player.generateVanishUser(): VelocityVanishUser {
            val player = this
            return VelocityVanishUser(player.uniqueId, player.username)
        }

        @JvmStatic
        fun VanishUser.velocityAdapt(): VelocityVanishUser {
            return VelocityPlatformAdapter.adapt(this)
        }
    }

}