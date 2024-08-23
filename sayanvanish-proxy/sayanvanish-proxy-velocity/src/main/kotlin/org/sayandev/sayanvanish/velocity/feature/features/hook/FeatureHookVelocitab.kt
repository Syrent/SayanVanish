package org.sayandev.sayanvanish.velocity.feature.features.hook

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.PostLoginEvent
import com.velocitypowered.api.event.player.ServerPostConnectEvent
import com.velocitypowered.api.event.player.ServerPreConnectEvent
import net.william278.velocitab.api.VelocitabAPI
import net.william278.velocitab.vanish.VanishIntegration
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.velocity.api.SayanVanishVelocityAPI.Companion.getOrCreateUser
import org.sayandev.sayanvanish.velocity.feature.HookFeature
import org.sayandev.sayanvanish.velocity.api.SayanVanishVelocityAPI.Companion.user
import org.sayandev.sayanvanish.velocity.event.VelocityUserUnVanishEvent
import org.sayandev.sayanvanish.velocity.event.VelocityUserVanishEvent
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.sayandev.stickynote.velocity.StickyNote
import org.sayandev.stickynote.velocity.registerListener
import org.sayandev.stickynote.velocity.warn

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
        val player = StickyNote.getPlayer(name) ?: return true
        val otherPlayer = StickyNote.getPlayer(otherName) ?: return true
        val user = player.getOrCreateUser()
        val otherUser = otherPlayer.getOrCreateUser()
        return if (isVanished(name) && isVanished(otherName) && user.vanishLevel >= otherUser.vanishLevel) true
        else if (isVanished(otherName)) false
        else true
    }

    override fun isVanished(name: String): Boolean {
        return StickyNote.getPlayer(name)?.user()?.isVanished == true
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

    @Subscribe
    private fun onServerPostConnect(event: ServerPostConnectEvent) {
        val player = event.player ?: return
        val user = player.user() ?: return
        if (user.isVanished) {
            VelocitabAPI.getInstance().vanishPlayer(player)
        } else {
            VelocitabAPI.getInstance().unVanishPlayer(player)
        }
    }

    @Subscribe
    private fun onPostLogin(event: PostLoginEvent) {
        val player = event.player ?: return
        val user = player.user() ?: return
        if (user.isVanished) {
            VelocitabAPI.getInstance().vanishPlayer(player)
        } else {
            VelocitabAPI.getInstance().unVanishPlayer(player)
        }
    }
}