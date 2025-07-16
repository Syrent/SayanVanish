package org.sayandev.sayanvanish.api.cache

interface ICache<K, V> : MutableMap<K, V> {
    val id: String
}