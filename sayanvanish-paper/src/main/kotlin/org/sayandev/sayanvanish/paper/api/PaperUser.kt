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
package org.sayandev.sayanvanish.paper.api

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Bukkit
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.sayandev.sayanvanish.api.Permissions
import org.sayandev.sayanvanish.api.User
import org.sayandev.sayanvanish.paper.utils.PermissionUtils.asBukkitPermissionDefault
import org.sayandev.sayanvanish.paper.utils.PlayerUtils.sendPrefixComponent
import org.sayandev.stickynote.bukkit.extension.sendComponent
import org.sayandev.stickynote.bukkit.extension.sendComponentActionbar
import org.sayandev.stickynote.bukkit.utils.AdventureUtils
import org.sayandev.stickynote.bukkit.utils.AdventureUtils.component
import java.util.*

class PaperUser(
    override val uniqueId: UUID,
    override var username: String,
    override var isOnline: Boolean,
    override var serverId: String
) : User {

    fun player() = Bukkit.getPlayer(uniqueId)

    fun audience(): Audience? = player()?.let { AdventureUtils.senderAudience(it) }

    override fun hasPermission(permission: String): Boolean {
        return player()?.hasPermission(Permission(permission, PermissionDefault.FALSE)) == true
    }

    override fun hasPermission(permission: Permissions): Boolean {
        return player()?.hasPermission(Permission(permission.permission(), permission.default.asBukkitPermissionDefault())) == true
    }

    override fun sendMessage(content: String, vararg placeholders: TagResolver) {
        sendMessage(content.component(*placeholders))
    }

    override fun sendActionbar(content: String, vararg placeholders: TagResolver) {
        sendActionbar(content.component(*placeholders))
    }

    fun sendMessage(content: Component) {
        player()?.sendComponent(content)
    }

    override fun sendMessageWithPrefix(content: String, vararg placeholders: TagResolver) {
        player()?.sendPrefixComponent(content, *placeholders)
    }

    fun sendActionbar(content: Component) {
        player()?.sendComponentActionbar(content)
    }

    companion object {
        @JvmSynthetic
        fun User.bukkitAdapt(): PaperUser {
            return PaperPlatformAdapter.adapt(this)
        }
    }
}