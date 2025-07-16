package org.sayandev.sayanvanish.api.message

enum class MessagingTypes(
    override val id: String
) : MessagingType {
    REDIS("redis"),
    WEBSOCKET("websocket"),
}