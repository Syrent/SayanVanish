package org.sayandev.sayanvanish.api

import org.sayandev.sayanvanish.api.database.PlatformTable
import java.util.*

class Queue(
    val uniqueId: UUID,
    val vanished: Boolean,
) {
    object Schema : PlatformTable("queue") {
        val uniqueId = reference("unique_id", User.Schema.uniqueId).uniqueIndex()
        val vanished = bool("vanished").default(false)

        override val primaryKey = PrimaryKey(uniqueId)
    }
}