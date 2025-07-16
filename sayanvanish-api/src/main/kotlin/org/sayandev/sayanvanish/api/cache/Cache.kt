package org.sayandev.sayanvanish.api.cache

class Cache<K, V>(
    override val id: String
) : ICache<K, V>, MutableMap<K, V> by mutableMapOf() {

    fun getValue(key: K): V? {
        return this[key]
    }
}