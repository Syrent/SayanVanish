package org.sayandev.sayanvanish.velocity.hook

import com.velocitypowered.api.event.Subscribe
import net.william278.velocitab.api.VelocitabAPI
import net.william278.velocitab.vanish.VanishIntegration
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.bukkit.feature.HookFeature
import org.sayandev.sayanvanish.velocity.api.SayanVanishVelocityAPI.Companion.user
import org.sayandev.sayanvanish.velocity.event.VelocityUserUnVanishEvent
import org.sayandev.sayanvanish.velocity.event.VelocityUserVanishEvent
import org.sayandev.stickynote.lib.spongepowered.configurate.objectmapping.ConfigSerializable
import org.sayandev.stickynote.velocity.StickyNote
import org.sayandev.stickynote.velocity.registerListener

@RegisteredFeature
@ConfigSerializable
class FeatureHookVelocitab : HookFeature("hook_velocitab", "velocitab") {
    override fun enable() {
        if (hasPlugin()) {
            VelocitabAPI.getInstance().vanishIntegration = VelocitabImpl()
        }
        super.enable()
    }
}

private class VelocitabImpl : VanishIntegration {

    init {
        registerListener(this)
    }

    override fun canSee(name: String, otherName: String): Boolean {
        val player = StickyNote.getPlayer(name)
        if (player == null) return true
        val otherPlayer = StickyNote.getPlayer(otherName)
        if (otherPlayer == null) return true
        val user = player.user(false) ?: return true
        val otherUser = otherPlayer.user(false) ?: return true
        return if (isVanished(name) && isVanished(otherName) && user.vanishLevel >= otherUser.vanishLevel) true
        else if (isVanished(otherName)) false
        else true
    }

    override fun isVanished(name: String): Boolean {
        return StickyNote.getPlayer(name)?.user(false)?.isVanished == true
    }

    @Subscribe
    private fun onVanish(event: VelocityUserVanishEvent) {
        val player = event.user.player() ?: return
        VelocitabAPI.getInstance().vanishPlayer(player)
        VelocitabAPI.getInstance().tabList.updateDisplayNames()
    }

    @Subscribe
    private fun onUnVanish(event: VelocityUserUnVanishEvent) {
        val player = event.user.player() ?: return
        VelocitabAPI.getInstance().unVanishPlayer(player)
    }
}