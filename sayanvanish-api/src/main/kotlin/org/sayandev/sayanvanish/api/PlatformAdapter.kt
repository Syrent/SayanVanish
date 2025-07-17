package org.sayandev.sayanvanish.api

interface PlatformAdapter<U: User, V: VanishUser> {
    fun adapt(user: User): U

    fun adapt(vanishUser: VanishUser): V
}