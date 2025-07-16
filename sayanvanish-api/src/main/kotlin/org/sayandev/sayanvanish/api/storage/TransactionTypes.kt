package org.sayandev.sayanvanish.api.storage

enum class TransactionTypes(
    override val id: String,
    override val type: DatabaseType,
) : TransactionType {
    USER("user", DatabaseType.SQL),
    VANISH_USER("vanish_user", DatabaseType.SQL),
}