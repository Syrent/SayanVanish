package org.sayandev.sayanvanish.api.database

enum class TransactionTypes(
    override val id: String,
    override val type: DatabaseType,
) : TransactionType {
    USER("user", DatabaseType.SQL),
    VANISH_USER("vanish_user", DatabaseType.SQL),
    // TODO: Change default to plugin message or implement WebSocket
    QUEUE("queue", DatabaseType.REDIS),
}