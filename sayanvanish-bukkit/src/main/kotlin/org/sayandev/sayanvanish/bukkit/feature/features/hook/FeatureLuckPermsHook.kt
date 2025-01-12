package org.sayandev.sayanvanish.bukkit.feature.features.hook

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
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.getOrCreateUser
import org.sayandev.sayanvanish.bukkit.feature.HookFeature
import org.sayandev.stickynote.bukkit.warn
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import java.util.*


@RegisteredFeature
@ConfigSerializable
class FeatureLuckPermsHook(
    @Configurable val registerCustomContext: Boolean = true,
    @Configurable val checkPermissionViaLuckPerms: Boolean = false,
): HookFeature("hook_luckperms", "LuckPerms") {

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

    override fun disable() {
        vanishContext?.let { LuckPermsProvider.get().contextManager.unregisterCalculator(it) }
        super.disable()
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
            return user.getInheritedGroups(QueryOptions.nonContextual()).any { it.data().contains(permissionNode, NodeEqualityPredicate.IGNORE_EXPIRY_TIME).asBoolean() }
        }
        return userPermission
    }
}

class VanishedContext: ContextCalculator<Player> {

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