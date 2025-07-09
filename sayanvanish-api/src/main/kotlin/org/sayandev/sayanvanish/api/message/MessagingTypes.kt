package org.sayandev.sayanvanish.api.message

enum class MessagingTypes(
    override val id: String
) : MessagingType {
    PLUGIN_MESSAGE("plugin_message"),
    REDIS("redis"),
}