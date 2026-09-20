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

import org.sayandev.sayanvanish.api.VanishUser
import org.sayandev.sayanvanish.api.cache.Cache
import org.sayandev.sayanvanish.api.cache.ICache
import java.util.*

class VanishUserCache: IVanishUserCache, ICache<UUID, VanishUser> by Cache("vanish_user") {
    override fun getVanishUser(uniqueId: UUID): VanishUser? {
        return this[uniqueId]
    }

    override fun hasVanishUser(uniqueId: UUID): Boolean {
        return this.containsKey(uniqueId)
    }

    override fun getOnline(): List<VanishUser> {
        return this.values.filter { it.isOnline }
    }

    override fun getByServer(serverId: String): List<VanishUser> {
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

    override fun getOnlineUsersByServerId(serverId: String): List<VanishUser> {
        return this.getByServer(serverId).filter { it.isOnline }
    }

    override fun getOnlineCountByServerId(serverId: String): Int {
        return this.getOnlineUsersByServerId(serverId).size
    }

    override fun getVanished(): List<VanishUser> {
        return this.values.filter { it.isVanished }
    }

    override fun getOnlineVanished(): List<VanishUser> {
        return this.getVanished().filter { it.isOnline }
    }

    override fun getVanishedCount(): Int {
        return this.getVanished().size
    }

    override fun getOnlineVanishedCount(): Int {
        return this.getOnlineVanished().size
    }
}