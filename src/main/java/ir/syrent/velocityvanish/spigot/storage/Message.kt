package ir.syrent.velocityvanish.spigot.storage

enum class Message(val path: String) {
    RAW_PREFIX("general.raw_prefix"),
    PREFIX("general.prefix"),
    CONSOLE_PREFIX("general.console_prefix"),
    SUCCESSFUL_PREFIX("general.successful_prefix"),
    WARN_PREFIX("general.warn_prefix"),
    ERROR_PREFIX("general.error_prefix"),
    ONLY_PLAYERS("general.only_players"),
    UNKNOWN_MESSAGE("general.unknown_message"),
    NO_PERMISSION("command.no_permission"),
    VANISH_USE_VANISH("command.vanish.use_vanish"),
    VANISH_USE_UNVANISH("command.vanish.use_unvanish"),
    RELOAD_USE("command.vanish.reload.use"),
    VANISH_ACTIONBAR("vanish.actionbar"),
    VANISH_NOTIFY("vanish.vanish_notify"),
    UNVANISH_NOTIFY("vanish.unvanish_notify"),
    JOIN_MESSAGE("vanish.join_message"),
    QUIT_MESSAGE("vanish.quit_message"),
    EMPTY("");
}