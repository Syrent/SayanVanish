package org.sayandev.sayanvanish.bukkit.feature.features.hook

import net.luckperms.api.LuckPerms
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.context.ContextCalculator
import net.luckperms.api.context.ContextConsumer
import net.luckperms.api.context.ContextSet
import net.luckperms.api.context.ImmutableContextSet
import org.bukkit.entity.Player
import org.sayandev.sayanvanish.api.feature.Configurable
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.getOrCreateUser
import org.sayandev.sayanvanish.bukkit.feature.HookFeature
import org.sayandev.stickynote.lib.spongepowered.configurate.objectmapping.ConfigSerializable


@RegisteredFeature
@ConfigSerializable
class FeatureLuckPermsHook(
    @Configurable val registerCustomContext: Boolean = true,
): HookFeature("hook_luckperms", "LuckPerms") {

    override fun enable() {
        if (hasPlugin()) {
            if (registerCustomContext) {
                LuckPermsProvider.get().contextManager.registerCalculator(VanishedContext())
            }
        }
        super.enable()
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