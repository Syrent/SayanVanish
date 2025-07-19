package org.sayandev.sayanvanish.api.storage

import kotlinx.serialization.Serializable

@Serializable
enum class DatabaseType {
    SQL,
    REDIS,
}