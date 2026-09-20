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
package org.sayandev.sayanvanish.velocity

import org.sayandev.sayanvanish.api.Platform
import org.sayandev.sayanvanish.api.PlatformAdapter
import org.sayandev.sayanvanish.api.User
import org.sayandev.sayanvanish.api.VanishUser
import org.sayandev.sayanvanish.velocity.api.VelocityUser
import org.sayandev.sayanvanish.velocity.api.VelocityVanishUser
import org.sayandev.stickynote.velocity.StickyNote
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

object VelocityPlatformAdapter : PlatformAdapter<VelocityUser, VelocityVanishUser> {
    override fun adapt(user: User): VelocityUser {
        val serverId = resolveServerId(user.uniqueId, runCatching { user.serverId }.getOrNull())
        return VelocityUser(user.uniqueId, user.username, user.isOnline, serverId)
    }

    override fun adapt(vanishUser: VanishUser): VelocityVanishUser {
        return VelocityVanishUser(vanishUser.uniqueId, vanishUser.username).also {
            it.currentOptions = vanishUser.currentOptions
            it.isVanished = vanishUser.isVanished
            it.vanishLevel = vanishUser.vanishLevel
        }
    }

    fun get(): VelocityPlatformAdapter {
        return this
    }

    private fun resolveServerId(uniqueId: UUID, serverId: String?): String {
        return serverId
            ?: StickyNote.getPlayer(uniqueId)?.currentServer?.getOrNull()?.serverInfo?.name?.takeUnless { it.isEmpty() }
            ?: Platform.get().serverId
    }
}
