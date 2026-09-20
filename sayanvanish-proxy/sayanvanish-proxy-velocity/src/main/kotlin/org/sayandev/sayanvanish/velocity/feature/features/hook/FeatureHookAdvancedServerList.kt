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
package org.sayandev.sayanvanish.velocity.feature.features.hook

import ch.andre601.advancedserverlist.api.AdvancedServerListAPI
import ch.andre601.advancedserverlist.api.PlaceholderProvider
import ch.andre601.advancedserverlist.api.exceptions.InvalidPlaceholderProviderException
import ch.andre601.advancedserverlist.api.objects.GenericPlayer
import ch.andre601.advancedserverlist.api.objects.GenericServer
import kotlinx.serialization.SerialName
import org.sayandev.sayanvanish.api.VanishAPI
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.proxy.config.language
import org.sayandev.sayanvanish.velocity.feature.HookFeature
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.sayandev.stickynote.velocity.registerListener

@RegisteredFeature
@Serializable
@SerialName("hook_advanced_server_list")
class FeatureHookAdvancedServerList : HookFeature() {

    @Transient override val id = "hook_advanced_server_list"
    override var enabled: Boolean = true
    override val plugin: String = "advancedserverlist"

    override fun enable() {
        if (hasPlugin()) {
            AdvancedServerListImpl()
        }
        super.enable()
    }
}

private class AdvancedServerListImpl : PlaceholderProvider("sayanvanish") {
    init {
        try {
            AdvancedServerListAPI.get()
                .addPlaceholderProvider(this)
        } catch (_: InvalidPlaceholderProviderException) { }
    }

    override fun parsePlaceholder(
        placeholder: String,
        player: GenericPlayer,
        server: GenericServer
    ): String? {
        if (placeholder.equals("vanished", true)) {
            return if (VanishAPI.get().getCacheService().getVanishUsers()[player.uuid]?.isVanished == true) "true" else "false"
        }

        if (placeholder.equals("level", true)) {
            return VanishAPI.get().getCacheService().getVanishUsers()[player.uuid]?.vanishLevel?.toString() ?: "0"
        }

        if (placeholder.equals("count", true)) {
            return VanishAPI.get().getCacheService().getVanishUsers().values.filter { user -> user.isOnline && user.isVanished }.size.toString()
        }

        if (placeholder.equals("vanish_prefix", true)) {
            return if (VanishAPI.get().getCacheService().getVanishUsers()[player.uuid]?.isVanished == true) language.vanish.placeholderPrefix else ""
        }

        if (placeholder.equals("vanish_suffix", true)) {
            return if (VanishAPI.get().getCacheService().getVanishUsers()[player.uuid]?.isVanished == true) language.vanish.placeholderSuffix else ""
        }

        if (placeholder.startsWith("online_")) {
            val type = placeholder.substring(7)
            val vanishedOnlineUsers = VanishAPI.get().getCacheService().getVanishUsers().values.filter { user -> user.isVanished && user.isOnline }

            return if (type.equals("total", true)) {
                VanishAPI.get().getCacheService().getUsers().size.minus(vanishedOnlineUsers.size).toString()
            } else {
                VanishAPI.get().getCacheService().getUsers().values.filter { it.serverId.equals(type, ignoreCase = true) }.toString()
            }
        }

        return null
    }

    init {
        registerListener(this)
    }
}