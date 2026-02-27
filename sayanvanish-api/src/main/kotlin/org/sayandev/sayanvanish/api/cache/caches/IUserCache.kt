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
package org.sayandev.sayanvanish.api.cache.caches

import org.sayandev.sayanvanish.api.User
import org.sayandev.sayanvanish.api.cache.ICache
import java.util.UUID

interface IUserCache : ICache<UUID, User> {
    fun getUser(uniqueId: UUID): User?
    fun getOnline(): List<User>
    fun getByServer(serverId: String): List<User>
    fun getCountByServer(serverId: String): Int
    fun getOnlineCountByServer(serverId: String): Int
    fun getOnlineCount(): Int
    fun getCount(): Int
    fun getOnlineUsersByServerId(serverId: String): List<User>
    fun getOnlineCountByServerId(serverId: String): Int
}