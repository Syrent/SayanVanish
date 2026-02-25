package org.sayandev.sayanvanish.api.storage.websocket

import kotlinx.serialization.Serializable
import com.charleskorn.kaml.YamlComment

@Serializable
class WebSocketConfig(
    val threadCount: Int = 5,
    val uri: String = "ws://localhost:8083",
    @YamlComment("If true, when no websocket server is listening on the configured URI port, this node will host one locally.")
    val autoHostOnPort: Boolean = true,
    @YamlComment("Bind address used when autoHostOnPort is enabled.")
    val hostWhenAutoHosting: String = "0.0.0.0"
)
