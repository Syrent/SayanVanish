package org.sayandev.sayanvanish.velocity.feature.features.hook

import com.velocitypowered.api.proxy.Player
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.context.ContextCalculator
import net.luckperms.api.context.ContextConsumer
import net.luckperms.api.context.ContextSet
import net.luckperms.api.context.ImmutableContextSet
import net.luckperms.api.node.NodeEqualityPredicate
import net.luckperms.api.node.types.PermissionNode
import net.luckperms.api.query.QueryOptions
import org.sayandev.sayanvanish.api.feature.Configurable
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.velocity.api.SayanVanishVelocityAPI.Companion.getOrCreateUser
import org.sayandev.sayanvanish.velocity.feature.HookFeature
import org.sayandev.stickynote.velocity.StickyNote
import org.sayandev.stickynote.velocity.warn
import kotlinx.serialization.Serializable
import com.charleskorn.kaml.YamlComment
import java.util.*

@RegisteredFeature
@Serializable
class FeatureLuckPermsHook(
    @YamlComment("Register custom context for vanished players, this will allow you to check if a player is vanished using LuckPerms.")
    @Configurable val registerCustomContext: Boolean = true,
    @YamlComment("Check permission using LuckPerms, if false, will fallback to velocity permission check.")
    @Configurable val checkPermissionViaLuckPerms: Boolean = true,
): HookFeature("hook_luckperms", "luckperms") {

    override fun enable() {
        if (hasPlugin()) {
            if (registerCustomContext) {
                LuckPermsProvider.get().contextManager.registerCalculator(VanishedContext())
            }
        }
        super.enable()
    }

    fun hasPermission(uniqueId: UUID, permission: String): Boolean {
        // Can't check permissions on a per-player basis to prevent stackoverflow
        if (!isActive()) {
            warn("tried to check permission using LuckPerms, but the `${this.id}` feature is not active, fallback to bukkit permission check.")
            return StickyNote.getPlayer(uniqueId)?.hasPermission(permission) == true
        }
        val user = LuckPermsProvider.get().userManager.getUser(uniqueId) ?: return false
        val permissionNode = PermissionNode.builder(permission).value(true).build()
        val userPermission = user
            .data()
            .contains(permissionNode, NodeEqualityPredicate.IGNORE_EXPIRY_TIME)
            .asBoolean()
        if (!userPermission) {
            return user.getInheritedGroups(QueryOptions.nonContextual()).any { it.data().contains(permissionNode,
                NodeEqualityPredicate.IGNORE_EXPIRY_TIME
            ).asBoolean() }
        }
        return userPermission
    }

    fun getPermissions(uniqueId: UUID): List<String> {
        val user = LuckPermsProvider.get().userManager.getUser(uniqueId) ?: return emptyList()
        return user
            .data()
            .toCollection()
            .filterIsInstance<PermissionNode>()
            .toMutableList()
            .plus(user.getInheritedGroups(QueryOptions.nonContextual()).flatMap { it.data().toCollection().filterIsInstance<PermissionNode>() })
            .filter { !it.hasExpired() && it.value }
            .map { it.permission }
    }
}

private class VanishedContext: ContextCalculator<Player> {

    override fun calculate(target: Player, contextConsumer: ContextConsumer) {
        contextConsumer.accept("vanished", target.getOrCreateUser().isVanished.toString())
    }

    override fun estimatePotentialContexts(): ContextSet {
        val builder = ImmutableContextSet.builder()
        builder.add("vanished", "true")
        builder.add("vanished", "false")
        return builder.build()
    }

}