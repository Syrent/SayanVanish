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
package org.sayandev.sayanvanish.velocity.api

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.sayandev.sayanvanish.api.Permissions
import org.sayandev.sayanvanish.api.User
import org.sayandev.sayanvanish.velocity.VelocityPlatformAdapter
import org.sayandev.sayanvanish.velocity.utils.PlayerUtils.sendPrefixComponent
import org.sayandev.stickynote.velocity.StickyNote
import org.sayandev.stickynote.velocity.utils.AdventureUtils.component
import java.util.*

open class VelocityUser(
    override val uniqueId: UUID,
    override var username: String,
    override var isOnline: Boolean,
    override var serverId: String
) : User {
    fun player() = StickyNote.getPlayer(uniqueId)

    override fun sendMessage(content: String, vararg placeholders: TagResolver) {
        player()?.sendMessage(content.component(*placeholders))
    }

    override fun sendMessageWithPrefix(content: String, vararg placeholders: TagResolver) {
        player()?.sendPrefixComponent(content.component(*placeholders))
    }

    override fun sendActionbar(content: String, vararg placeholders: TagResolver) {
        player()?.sendActionBar(content.component(*placeholders))
    }

    override fun hasPermission(permission: String): Boolean {
        return player()?.hasPermission(permission) ?: false
    }

    override fun hasPermission(permission: Permissions): Boolean {
        return player()?.hasPermission(permission.permission()) ?: false
    }

    companion object {
        @JvmSynthetic
        fun User.velocityAdapt(): VelocityUser {
            return VelocityPlatformAdapter.adapt(this)
        }
    }
}