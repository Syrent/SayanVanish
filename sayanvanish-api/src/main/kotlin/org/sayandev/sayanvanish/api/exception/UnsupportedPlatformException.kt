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
package org.sayandev.sayanvanish.api.exception

import org.sayandev.sayanvanish.api.Platform

class UnsupportedPlatformException(action: String?, message: String) : Exception(message) {
    constructor(action: String) : this(action, "This action is not supported on this platform yet. (platform: ${Platform.get().id}, action: ${action})")
    constructor() : this(null, "This platform is not supported yet. (platform: ${Platform.get().id})")
}