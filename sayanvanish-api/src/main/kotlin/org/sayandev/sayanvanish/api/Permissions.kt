package org.sayandev.sayanvanish.api

enum class Permissions(val value: String, val default: PermissionDefaults = PermissionDefaults.FALSE) {
    VANISH("vanish.use", PermissionDefaults.ADMIN),
    VANISH_OTHERS("commands.vanish.others"),
    VANISH_ON_JOIN("action.vanish.onjoin"),
    FEATURE_PLAYER_TOGGLE("commands.feature.toggleplayer.other"),
    INVULNERABLE("action.vanish.invulnerable"),
    FLY_KEEP_AFTER_REAPPEAR("action.vanish.fly.keep_after_reappear", PermissionDefaults.TRUE),
    FLY("action.vanish.fly", PermissionDefaults.ADMIN);

    fun permission(): String {
        return "${Platform.get().pluginName.lowercase()}.${value}"
    }
}

enum class PermissionDefaults {
    TRUE,
    FALSE,
    ADMIN,
    NOT_ADMIN,
}