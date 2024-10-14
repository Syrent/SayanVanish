package org.sayandev.sayanvanish.velocity.feature.features.hook

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.PostLoginEvent
import com.velocitypowered.api.event.player.ServerPostConnectEvent
import com.velocitypowered.api.proxy.Player
import net.william278.velocitab.api.VelocitabAPI
import net.william278.velocitab.vanish.VanishIntegration
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.velocity.api.SayanVanishVelocityAPI
import org.sayandev.sayanvanish.velocity.api.SayanVanishVelocityAPI.Companion.getOrCreateUser
import org.sayandev.sayanvanish.velocity.api.SayanVanishVelocityAPI.Companion.user
import org.sayandev.sayanvanish.velocity.event.VelocityUserUnVanishEvent
import org.sayandev.sayanvanish.velocity.event.VelocityUserVanishEvent
import org.sayandev.sayanvanish.velocity.feature.HookFeature
import org.sayandev.stickynote.velocity.StickyNote
import org.sayandev.stickynote.velocity.registerListener
import org.spongepowered.configurate.objectmapping.ConfigSerializable

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
        return if (user.isVanished && otherUser.isVanished && user.vanishLevel >= otherUser.vanishLevel) true
        else if (otherUser.isVanished) false
        else true
    }

    override fun isVanished(name: String): Boolean {
        return StickyNote.getPlayer(name)?.getOrCreateUser()?.isVanished == true
    }

    @Subscribe
    private fun onVanish(event: VelocityUserVanishEvent) {
        val player = event.user.player() ?: return
        vanish(player)
    }

    @Subscribe
    private fun onUnVanish(event: VelocityUserUnVanishEvent) {
        val player = event.user.player() ?: return
        unVanish(player)
    }

    @Subscribe
    private fun onServerPostConnect(event: ServerPostConnectEvent) {
        val player = event.player ?: return
        for (vanishedUser in SayanVanishVelocityAPI.getInstance().getVanishedUsers()) {
            val vanishedPlayer = vanishedUser.player() ?: continue
            vanish(vanishedPlayer)
        }

        val user = player.user() ?: return
        if (user.isVanished) {
            vanish(player)
        } else {
            unVanish(player)
        }
    }

    @Subscribe
    private fun onPostLogin(event: PostLoginEvent) {
        val player = event.player ?: return
        for (vanishedUser in SayanVanishVelocityAPI.getInstance().getVanishedUsers()) {
            val vanishedPlayer = vanishedUser.player() ?: continue
            vanish(vanishedPlayer)
        }

        val user = player.user() ?: return
        if (user.isVanished) {
            vanish(player)
        } else {
            unVanish(player)
        }
    }

    private fun vanish(player: Player) {
        val tabPlayer = VelocitabAPI.getInstance().tabList.getTabPlayer(player.uniqueId)
        if (tabPlayer.isEmpty) return
        VelocitabAPI.getInstance().vanishPlayer(player)
    }

    private fun unVanish(player: Player) {
        val tabPlayer = VelocitabAPI.getInstance().tabList.getTabPlayer(player.uniqueId)
        if (tabPlayer.isEmpty) return
        VelocitabAPI.getInstance().unVanishPlayer(player)
    }
}