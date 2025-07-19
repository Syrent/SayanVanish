package org.sayandev.sayanvanish.api.storage.websocket

import kotlinx.serialization.Serializable
import com.charleskorn.kaml.YamlComment

@Serializable
class WebSocketConfig(
    val threadCount: Int = 5,
    val uri: String = "ws://localhost:8083"
)