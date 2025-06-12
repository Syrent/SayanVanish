package org.sayandev.sayanvanish.api

import org.jetbrains.exposed.v1.core.Table
import java.util.UUID

class Queue(
    val uniqueId: UUID,
    val vanished: Boolean,
) {
    object Schema : Table("${Platform.get().pluginName.lowercase()}_queue") {
        val uniqueId = reference("unique_id", User.Schema.uniqueId).uniqueIndex()
        val vanished = bool("vanished").default(false)

        override val primaryKey = PrimaryKey(uniqueId)
    }
}