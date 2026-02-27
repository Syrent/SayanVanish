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

import com.charleskorn.kaml.YamlComment
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.PostLoginEvent
import com.velocitypowered.api.event.player.ServerConnectedEvent
import com.velocitypowered.api.event.player.ServerPostConnectEvent
import com.velocitypowered.api.proxy.Player
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.william278.velocitab.api.VelocitabAPI
import net.william278.velocitab.vanish.VanishIntegration
import org.sayandev.sayanvanish.api.VanishAPI
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.velocity.api.SayanVanishVelocityAPI.Companion.cachedVanishUser
import org.sayandev.sayanvanish.velocity.api.SayanVanishVelocityAPI.Companion.getCachedOrCreateVanishUser
import org.sayandev.sayanvanish.velocity.api.VelocityVanishUser.Companion.velocityAdapt
import org.sayandev.sayanvanish.velocity.event.VelocityUserUnVanishEvent
import org.sayandev.sayanvanish.velocity.event.VelocityUserVanishEvent
import org.sayandev.sayanvanish.velocity.feature.HookFeature
import org.sayandev.stickynote.velocity.StickyNote
import org.sayandev.stickynote.velocity.registerListener
import java.util.concurrent.TimeUnit

@RegisteredFeature
@Serializable
@SerialName("hook_velocitab")
class FeatureHookVelocitab(
    @YamlComment("The delay in milliseconds to check on post server connect event. low values may cause issues.")
    val checkOnPostServerConnectDelay: Long = 150,
    @YamlComment("The delay in milliseconds to check on server switch. low values may cause issues.")
    val checkOnServerConnectedDelay: Long = 150,
    @YamlComment("The delay in milliseconds to check on post login event. low values may cause issues.")
    val checkOnPostLoginDelay: Long = 150,
) : HookFeature() {

    @Transient override val id = "hook_velocitab"
    override var enabled: Boolean = true
    override val plugin: String = "velocitab"

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
        val user = player.getCachedOrCreateVanishUser()
        val otherUser = otherPlayer.getCachedOrCreateVanishUser()
        return if (user.isVanished && otherUser.isVanished && user.vanishLevel >= otherUser.vanishLevel) true
        else if (otherUser.isVanished) false
        else true
    }

    override fun isVanished(name: String): Boolean {
        return StickyNote.getPlayer(name)?.let { VanishAPI.get().getCacheService().getVanishUsers().values.find { it.username == name } }?.isVanished == true
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
            for (vanishedUser in VanishAPI.get().getCacheService().getVanishUsers().values.map { it.velocityAdapt() }) {
                val vanishedPlayer = vanishedUser.player() ?: continue
                vanish(vanishedPlayer)
            }

            val user = player.cachedVanishUser() ?: return@run
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
            for (vanishedUser in VanishAPI.get().getCacheService().getVanishUsers().values.map { it.velocityAdapt() }) {
                val vanishedPlayer = vanishedUser.player() ?: continue
                vanish(vanishedPlayer)
            }

            val user = player.cachedVanishUser() ?: return@run
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
            for (vanishedUser in VanishAPI.get().getCacheService().getVanishUsers().values.map { it.velocityAdapt() }) {
                val vanishedPlayer = vanishedUser.player() ?: continue
                vanish(vanishedPlayer)
            }

            val user = player.cachedVanishUser() ?: return@run
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