package org.sayandev.sayanvanish.api.message

enum class MessagingCategoryTypes(
    override val id: String,
    override val type: MessagingTypes,
) : MessagingCategoryType {
    // TODO: Change default to plugin message or implement WebSocket
    SYNC_USER("sync_user", MessagingTypes.REDIS),
    SYNC_VANISH_USER("sync_vanish_user", MessagingTypes.REDIS),
}