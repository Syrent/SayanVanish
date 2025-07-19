package org.sayandev.sayanvanish.api.message

import kotlinx.serialization.Serializable

@Serializable
enum class MessagingCategoryTypes(
    override val id: String,
    override val type: MessagingTypes,
) : MessagingCategoryType {
    SYNC_USER("sync_user", MessagingTypes.WEBSOCKET),
    SYNC_VANISH_USER("sync_vanish_user", MessagingTypes.WEBSOCKET),
}