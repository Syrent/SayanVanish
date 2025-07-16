package org.sayandev.sayanvanish.api

import net.kyori.adventure.text.Component

interface PlatformAdapter<U: User, V: VanishUser> {
    fun adapt(user: VanishUser): V

    fun sendMessage(user: U, message: Component)
}