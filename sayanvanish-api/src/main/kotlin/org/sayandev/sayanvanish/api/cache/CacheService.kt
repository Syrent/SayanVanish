package org.sayandev.sayanvanish.api.cache

import org.sayandev.sayanvanish.api.User
import java.util.UUID

// TODO: this is not what i want from a cache system. i want each cache to have its own unique methods. for example i want the getUsers method to have a getUsers().getOnline() sub method. so its probably better to create a seperated class for each type of cache
interface CacheService {
    fun getUsers(): ICache<UUID, User>
    fun getVanishUsers(): ICache<UUID, User>
    fun getUsersCount(): ICache<String, Int>
}