package org.sayandev.sayanvanish.api

enum class Permission(val value: String) {
    VANISH("vanish.use"),
    VANISH_OTHERS("commands.vanish.others"),
    VANISH_ON_JOIN("action.vanish.onjoin"),
    FEATURE_PLAYER_TOGGLE("commands.feature.toggleplayer.other"),
    INVULNERABLE("action.vanish.invulnerable"),
    FLY_KEEP_AFTER_REAPPEAR("action.vanish.fly.keep_after_reappear"),
    FLY("action.vanish.fly");

    fun permission(): String {
        return "${Platform.get().pluginName.lowercase()}.${value}"
    }
}