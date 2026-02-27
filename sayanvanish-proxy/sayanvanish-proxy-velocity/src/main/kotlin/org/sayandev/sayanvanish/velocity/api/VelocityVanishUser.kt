/*
 * This file is part of SayanVanish, licensed under the GNU General Public License v3.0.
 *
 * Copyright (c) 2026 Sayan Development and contributors
 *
 * SayanVanish is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SayanVanish is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.sayandev.sayanvanish.velocity.api

import com.velocitypowered.api.proxy.Player
import org.sayandev.sayanvanish.api.Platform
import org.sayandev.sayanvanish.api.Permissions
import org.sayandev.sayanvanish.api.VanishAPI
import org.sayandev.sayanvanish.api.VanishOptions
import org.sayandev.sayanvanish.api.VanishUser
import org.sayandev.sayanvanish.api.feature.Features
import org.sayandev.sayanvanish.velocity.VelocityPlatformAdapter
import org.sayandev.sayanvanish.velocity.feature.features.hook.FeatureHookLuckPerms
import org.sayandev.stickynote.velocity.StickyNote
import java.util.*
import kotlin.jvm.optionals.getOrNull

open class VelocityVanishUser(
    override val uniqueId: UUID,
    override var username: String
) : VanishUser {

    override var serverId: String = Platform.get().serverId
        get() = player()?.currentServer?.getOrNull()?.serverInfo?.name ?: field
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
                            ?: if (hasPermission(Permissions.VANISH)) 1 else {
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
