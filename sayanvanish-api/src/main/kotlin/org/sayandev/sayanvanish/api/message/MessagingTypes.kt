package org.sayandev.sayanvanish.api.message

import kotlinx.serialization.Serializable

@Serializable
enum class MessagingTypes(
    override val id: String
) : MessagingType {
    REDIS("redis"),
    WEBSOCKET("websocket"),
}