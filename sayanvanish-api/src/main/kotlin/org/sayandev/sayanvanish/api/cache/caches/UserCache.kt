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
import org.sayandev.sayanvanish.api.cache.Cache
import org.sayandev.sayanvanish.api.cache.ICache
import java.util.UUID

class UserCache: IUserCache, ICache<UUID, User> by Cache("users") {
    override fun getUser(uniqueId: UUID): User? {
        return this[uniqueId]
    }

    override fun getOnline(): List<User> {
        return this.values.filter { it.isOnline }
    }

    override fun getByServer(serverId: String): List<User> {
        return this.values.filter { it.serverId == serverId }
    }

    override fun getCountByServer(serverId: String): Int {
        return this.getByServer(serverId).size
    }

    override fun getOnlineCountByServer(serverId: String): Int {
        return this.getByServer(serverId).count { it.isOnline }
    }

    override fun getOnlineCount(): Int {
        return this.values.count { it.isOnline }
    }

    override fun getCount(): Int {
        return this.values.size
    }

    override fun getOnlineUsersByServerId(serverId: String): List<User> {
        return this.getByServer(serverId).filter { it.isOnline }
    }

    override fun getOnlineCountByServerId(serverId: String): Int {
        return this.getOnlineUsersByServerId(serverId).size
    }
}