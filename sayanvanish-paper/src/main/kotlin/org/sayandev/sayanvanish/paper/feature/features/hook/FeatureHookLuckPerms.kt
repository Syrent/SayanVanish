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
package org.sayandev.sayanvanish.paper.feature.features.hook

import com.charleskorn.kaml.YamlComment
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.context.ContextCalculator
import net.luckperms.api.context.ContextConsumer
import net.luckperms.api.context.ContextSet
import net.luckperms.api.context.ImmutableContextSet
import net.luckperms.api.node.NodeEqualityPredicate
import net.luckperms.api.node.types.PermissionNode
import net.luckperms.api.query.QueryOptions
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.sayandev.sayanvanish.api.feature.Configurable
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.paper.api.SayanVanishPaperAPI.Companion.getCachedOrCreateVanishUser
import org.sayandev.sayanvanish.paper.feature.HookFeature
import org.sayandev.stickynote.paper.warn
import java.util.*

@RegisteredFeature
@Serializable
@SerialName("hook_luckperms")
class FeatureHookLuckPerms(
    @YamlComment(
    "Whether to register custom context for vanished players.",
    "This will allow you to check if a player is vanished using the context \"vanished\".",
    "This is useful for checking permissions based on the player's vanish status.",
    )
    @Configurable val registerCustomContext: Boolean = true,
    @YamlComment("Whether to check permission using LuckPerms. If false, it will fallback to bukkit permission check.")
    @Configurable val checkPermissionViaLuckPerms: Boolean = false,
    @Configurable val checkPermissionViaLuckPermsFeatures: Boolean = true,
): HookFeature() {

    @Transient override val id = "hook_luckperms"
    override var enabled: Boolean = true
    override val plugin: String = "LuckPerms"

    @Transient var vanishContext: VanishedContext? = null

    override fun enable() {
        if (hasPlugin()) {
            if (registerCustomContext) {
                vanishContext = VanishedContext()
                LuckPermsProvider.get().contextManager.registerCalculator(vanishContext!!)
            }
        }
        super.enable()
    }

    override fun disable(reload: Boolean) {
        vanishContext?.let { LuckPermsProvider.get().contextManager.unregisterCalculator(it) }
        super.disable(reload)
    }

    fun hasPermission(uniqueId: UUID, permission: String): Boolean {
        // Can't check permissions on a per-player basis to prevent stackoverflow
        if (!isActive()) {
            warn("tried to check permission using LuckPerms, but the `${this.id}` feature is not active, fallback to bukkit permission check.")
            return Bukkit.getPlayer(uniqueId)?.hasPermission(permission) == true
        }
        val user = LuckPermsProvider.get().userManager.getUser(uniqueId) ?: return false
        val permissionNode = PermissionNode.builder(permission).value(true).build()
        val userPermission = user
            .data()
            .contains(permissionNode, NodeEqualityPredicate.IGNORE_EXPIRY_TIME)
            .asBoolean()
        if (!userPermission) {
            return user.getInheritedGroups(QueryOptions.defaultContextualOptions()).any { it.data().contains(permissionNode, NodeEqualityPredicate.IGNORE_EXPIRY_TIME).asBoolean() }
        }
        return userPermission
    }
}

class VanishedContext: ContextCalculator<Player> {

    override fun calculate(target: Player, contextConsumer: ContextConsumer) {
        contextConsumer.accept("vanished", target.getCachedOrCreateVanishUser().isVanished.toString())
    }

    override fun estimatePotentialContexts(): ContextSet {
        val builder = ImmutableContextSet.builder()
        builder.add("vanished", "true")
        builder.add("vanished", "false")
        return builder.build()
    }
}