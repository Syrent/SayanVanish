package org.sayandev.sayanvanish.api.cache

class UserCountCache : Cache<String, Int>("user_count") {
    fun totalCount(): Int {
        return values.sumOf { it }
    }
}