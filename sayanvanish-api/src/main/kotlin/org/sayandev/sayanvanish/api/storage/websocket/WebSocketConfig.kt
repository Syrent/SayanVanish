/*
 * This file is part of SayanVanish, licensed under the GNU General Public License v3.0.
 *
 * Copyright (c) 2026 Sayan Development and contributors
 *
 * SayanVanish is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SayanVanish is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
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
