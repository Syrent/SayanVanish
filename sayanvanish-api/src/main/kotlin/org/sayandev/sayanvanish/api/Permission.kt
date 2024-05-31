package org.sayandev.sayanvanish.api

enum class Permission(val value: String) {
    VANISH("vanish.use"),
    VANISH_ON_JOIN("action.vanish.onjoin"),
    INVULNERABLE("action.vanish.invulnerable"),
    FLY("action.vanish.fly");

    fun permission(): String {
        return "sayanvanish.${value}"
    }
}