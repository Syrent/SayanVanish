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
package org.sayandev.sayanvanish.paper.api

import org.sayandev.sayanvanish.api.PlatformAdapter
import org.sayandev.sayanvanish.api.Platform
import org.sayandev.sayanvanish.api.User
import org.sayandev.sayanvanish.api.VanishUser
import org.sayandev.sayanvanish.paper.config.Settings

object PaperPlatformAdapter : PlatformAdapter<PaperUser, PaperVanishUser> {
    override fun adapt(user: User): PaperUser {
        val serverId = if (Settings.get().general.proxyMode) {
            runCatching { user.serverId }.getOrNull() ?: Platform.get().serverId
        } else {
            Platform.get().serverId
        }
        return PaperUser(user.uniqueId, user.username, user.isOnline, serverId)
    }

    override fun adapt(vanishUser: VanishUser): PaperVanishUser {
        val serverId = if (Settings.get().general.proxyMode) {
            runCatching { vanishUser.serverId }.getOrNull() ?: Platform.get().serverId
        } else {
            Platform.get().serverId
        }
        return PaperVanishUser(vanishUser.uniqueId, vanishUser.username).also {
            it.serverId = serverId
            it.currentOptions = vanishUser.currentOptions
            it.isVanished = vanishUser.isVanished
            it.vanishLevel = vanishUser.vanishLevel
        }
    }

    fun get(): PaperPlatformAdapter {
        return this
    }
}
