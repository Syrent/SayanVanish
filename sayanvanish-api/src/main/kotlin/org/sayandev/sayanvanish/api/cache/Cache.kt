package org.sayandev.sayanvanish.api.cache

abstract class Cache<K, V>(
    id: String
) : MutableMap<K, V> by mutableMapOf() {
    fun getValue(key: K): V? {
        return this[key]
    }
}