package org.sayandev.sayanvanish.velocity.feature.features.hook

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.PostLoginEvent
import com.velocitypowered.api.event.player.ServerConnectedEvent
import com.velocitypowered.api.event.player.ServerPostConnectEvent
import com.velocitypowered.api.proxy.Player
import net.william278.velocitab.api.VelocitabAPI
import net.william278.velocitab.vanish.VanishIntegration
import org.sayandev.sayanvanish.api.VanishAPI
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.velocity.api.SayanVanishVelocityAPI
import org.sayandev.sayanvanish.velocity.api.VelocityVanishUser.Companion.generateVanishUser
import org.sayandev.sayanvanish.velocity.api.VelocityVanishUser.Companion.getVanishUser
import org.sayandev.sayanvanish.velocity.event.VelocityUserUnVanishEvent
import org.sayandev.sayanvanish.velocity.event.VelocityUserVanishEvent
import org.sayandev.sayanvanish.velocity.feature.HookFeature
import org.sayandev.stickynote.velocity.StickyNote
import org.sayandev.stickynote.velocity.registerListener
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment
import java.util.concurrent.TimeUnit

@RegisteredFeature
@ConfigSerializable
class FeatureHookVelocitab(
    @Comment("The delay in milliseconds to check on post server connect event. low values may cause issues.")
    val checkOnPostServerConnectDelay: Long = 150,
    @Comment("The delay in milliseconds to check on server switch. low values may cause issues.")
    val checkOnServerConnectedDelay: Long = 150,
    @Comment("The delay in milliseconds to check on post login event. low values may cause issues.")
    val checkOnPostLoginDelay: Long = 150,
) : HookFeature("hook_velocitab", "velocitab") {
    override fun enable() {
        if (hasPlugin()) {
            VelocitabAPI.getInstance().vanishIntegration = VelocitabImpl(this)
        }
        super.enable()
    }
}

private class VelocitabImpl(val feature: FeatureHookVelocitab) : VanishIntegration {

    init {
        registerListener(this)
    }

    override fun canSee(name: String, otherName: String): Boolean {
        val player = StickyNote.getPlayer(name) ?: return true
        val otherPlayer = StickyNote.getPlayer(otherName) ?: return true
        val user = VanishAPI.get().getDatabase().getVanishUserCache(player.uniqueId) ?: player.generateVanishUser()
        val otherUser = VanishAPI.get().getDatabase().getVanishUserCache(otherPlayer.uniqueId) ?: otherPlayer.generateVanishUser()
        return if (user.isVanished && otherUser.isVanished && user.vanishLevel >= otherUser.vanishLevel) true
        else if (otherUser.isVanished) false
        else true
    }

    override fun isVanished(name: String): Boolean {
        return StickyNote.getPlayer(name)?.let { VanishAPI.get().getDatabase().getCachedVanishUsers().values.find { it.username == name } }?.isVanished == true
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
        StickyNote.run({
            for (vanishedUser in SayanVanishVelocityAPI.getInstance().getVanishedUsers()) {
                val vanishedPlayer = vanishedUser.player() ?: continue
                vanish(vanishedPlayer)
            }

            val user = player.user() ?: return@run
            if (user.isVanished) {
                vanish(player)
            } else {
                unVanish(player)
            }
        }, feature.checkOnPostServerConnectDelay, TimeUnit.MILLISECONDS)
    }

    @Subscribe
    private fun onServerConnected(event: ServerConnectedEvent) {
        val player = event.player ?: return
        StickyNote.run({
            for (vanishedUser in SayanVanishVelocityAPI.getInstance().getVanishedUsers()) {
                val vanishedPlayer = vanishedUser.player() ?: continue
                vanish(vanishedPlayer)
            }

            val user = player.user() ?: return@run
            if (user.isVanished) {
                vanish(player)
            } else {
                unVanish(player)
            }
        }, feature.checkOnServerConnectedDelay, TimeUnit.MILLISECONDS)
    }

    @Subscribe
    private fun onPostLogin(event: PostLoginEvent) {
        val player = event.player ?: return
        StickyNote.run({
            for (vanishedUser in SayanVanishVelocityAPI.getInstance().getVanishedUsers()) {
                val vanishedPlayer = vanishedUser.player() ?: continue
                vanish(vanishedPlayer)
            }

            val user = player.user() ?: return@run
            if (user.isVanished) {
                vanish(player)
            } else {
                unVanish(player)
            }
        }, feature.checkOnPostLoginDelay, TimeUnit.MILLISECONDS)
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