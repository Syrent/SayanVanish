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
package org.sayandev.sayanvanish.api

import org.jetbrains.exposed.sql.ReferenceOption
import org.sayandev.sayanvanish.api.storage.PlatformTable

data class VanishOptions(
    var sendMessage: Boolean = true,
    var notifyStatusChangeToOthers: Boolean = true,
    var notifyJoinQuitVanished: Boolean = true,
    var isOnJoin: Boolean = false,
    var isOnQuit: Boolean = false,
) {

    object Schema : PlatformTable("vanish_user_options") {
        val uniqueId = reference(
            "unique_id",
            VanishUser.Schema.uniqueId,
            onDelete = ReferenceOption.CASCADE
        ).uniqueIndex()
        val sendMessage = bool("send_message").default(true)
        val notifyStatusChangeToOthers = bool("notify_status_change_to_others").default(true)
        val notifyJoinQuitVanished = bool("notify_join_quit_vanished").default(true)
        val isOnJoin = bool("is_on_join").default(false)
        val isOnQuit = bool("is_on_quit").default(false)

        override val primaryKey = PrimaryKey(uniqueId)
    }

    class Builder {
        private var sendMessage = true
        private var notifyStatusChangeToOthers = true
        private var notifyJoinQuitVanished = true
        private var isOnJoin = false
        private var isOnQuit = false

        fun sendMessage(sendMessage: Boolean): Builder {
            this.sendMessage = sendMessage
            return this
        }

        fun notifyStatusChangeToOthers(notifyStatusChangeToOthers: Boolean): Builder {
            this.notifyStatusChangeToOthers = notifyStatusChangeToOthers
            return this
        }

        fun notifyJoinQuitVanished(notifyJoinQuitVanished: Boolean): Builder {
            this.notifyJoinQuitVanished = notifyJoinQuitVanished
            return this
        }

        fun isOnJoin(isOnJoin: Boolean): Builder {
            this.isOnJoin = isOnJoin
            return this
        }

        fun isOnQuit(isOnQuit: Boolean): Builder {
            this.isOnQuit = isOnQuit
            return this
        }

        fun build(): VanishOptions {
            return VanishOptions(sendMessage, notifyStatusChangeToOthers, notifyJoinQuitVanished, isOnJoin, isOnQuit)
        }
    }

    companion object {
        @JvmStatic
        fun defaultOptions(): VanishOptions {
            return VanishOptions()
        }
    }
}
