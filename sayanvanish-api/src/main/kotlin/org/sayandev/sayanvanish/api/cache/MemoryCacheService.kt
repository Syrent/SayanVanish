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
package org.sayandev.sayanvanish.api.cache

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import org.sayandev.sayanvanish.api.User
import org.sayandev.sayanvanish.api.VanishUser
import org.sayandev.sayanvanish.api.storage.Database
import org.sayandev.sayanvanish.api.cache.caches.IUserCache
import org.sayandev.sayanvanish.api.cache.caches.IVanishUserCache
import org.sayandev.sayanvanish.api.cache.caches.UserCache
import org.sayandev.sayanvanish.api.cache.caches.VanishUserCache

class MemoryCacheService : CacheService {
    override suspend fun initialize(database: Database): Deferred<Boolean> {
        Users.clear()
        VanishUsers.clear()

        Users.putAll(database.getUsers().await().filter { it.isOnline }.associateBy(User::uniqueId))
        VanishUsers.putAll(database.getVanishUsers().await().associateBy(VanishUser::uniqueId))
        return CompletableDeferred(true)
    }

    override suspend fun clear(): Deferred<Boolean> {
        Users.clear()
        VanishUsers.clear()
        return CompletableDeferred(true)
    }

    object Users : IUserCache by UserCache()
    object VanishUsers : IVanishUserCache by VanishUserCache()

    override fun getUsers() = Users
    override fun getVanishUsers() = VanishUsers
}
