package org.sayandev.sayanvanish.api.storage.websocket

import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment

@ConfigSerializable
class WebSocketConfig(
    val threadCount: Int = 5,
    val uri: String = "ws://localhost:8083"
)