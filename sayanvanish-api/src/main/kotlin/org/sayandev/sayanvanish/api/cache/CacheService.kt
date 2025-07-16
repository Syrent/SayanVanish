package org.sayandev.sayanvanish.api.cache

import org.sayandev.sayanvanish.api.User
import java.util.UUID

interface CacheService {
    fun getUsers(): ICache<UUID, User>
    fun getVanishUsers(): ICache<UUID, User>
    fun getUsersCount(): ICache<String, Int>
}