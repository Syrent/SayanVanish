package org.sayandev.sayanvanish.api

interface PlatformAdapter<V: VanishUser> {
    fun adapt(user: VanishUser): V
}