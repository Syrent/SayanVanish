package org.sayandev.sayanvanish.bukkit.feature.features

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.sayandev.sayanvanish.api.Permission
import org.sayandev.sayanvanish.api.VanishOptions
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.getOrCreateUser
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.user
import org.sayandev.sayanvanish.bukkit.config.settings
import org.sayandev.sayanvanish.bukkit.feature.ListenedFeature
import org.sayandev.stickynote.lib.spongepowered.configurate.objectmapping.ConfigSerializable

@RegisteredFeature
@ConfigSerializable
class FeatureState(
    val remember: Boolean = true,
    val vanishOnJoin: Boolean = false,
    val reappearOnQuit: Boolean = false,
    val checkPermissionOnQuit: Boolean = false,
    val checkPermissionOnJoin: Boolean = false,
    val getFromJoinEvent: Boolean = true,
    val getFromQuitEvent: Boolean = true,
) : ListenedFeature("state") {

    @Transient var generalJoinMessage: String? = null
    @Transient var generalQuitMessage: String? = null

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onUnVanish(event: PlayerJoinEvent) {
        if (!isActive()) return
        val player = event.player
        val user = player.user(false)
        val vanishJoinOptions = VanishOptions.Builder().sendMessage(false).notifyOthers(false).build()

        if (user == null) {
            val tempUser = player.getOrCreateUser()

            if (checkPermissionOnQuit && !tempUser.hasPermission(Permission.VANISH)) {
                return
            }

            if (tempUser.hasPermission(Permission.VANISH_ON_JOIN) || vanishOnJoin) {
                tempUser.isOnline = true
                tempUser.isVanished = true
                tempUser.vanish(vanishJoinOptions)
                tempUser.save()
            }
            return
        }

        user.isOnline = true

        if (checkPermissionOnJoin && !user.hasPermission(Permission.VANISH)) {
            user.unVanish()
            user.save()
            return
        }

        if (user.hasPermission(Permission.VANISH_ON_JOIN) || (user.isVanished && remember) || vanishOnJoin) {
            user.isVanished = true
            user.vanish(vanishJoinOptions)
        }
        user.save()

        if (player.user(false)?.isVanished == true) {
            if (getFromJoinEvent) {
                generalJoinMessage = event.joinMessage
            }
            event.joinMessage = REMOVAL_MESSAGE_ID
        }
        return
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun updateUserOnQuit(event: PlayerQuitEvent) {
        val player = event.player
        val user = player.user() ?: return
        if ((reappearOnQuit && user.isVanished) || (checkPermissionOnQuit && !user.hasPermission(Permission.VANISH))) {
            user.unVanish()
        }
        user.isOnline = false
        user.save()

        if (user.isVanished) {
            if (getFromQuitEvent) {
                generalQuitMessage = event.quitMessage
            }
            event.quitMessage = REMOVAL_MESSAGE_ID
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun setJoinMessage(event: PlayerJoinEvent) {
        if (event.joinMessage != REMOVAL_MESSAGE_ID) {
            if (getFromJoinEvent) {
                generalJoinMessage = event.joinMessage
            }
        } else {
            event.joinMessage = null
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun setQuitMessage(event: PlayerQuitEvent) {
        if (event.quitMessage != REMOVAL_MESSAGE_ID) {
            if (getFromQuitEvent) {
                generalQuitMessage = event.quitMessage
            }
        } else {
            event.quitMessage = null
        }
    }

    companion object {
        private const val REMOVAL_MESSAGE_ID = "SAYANVANISH_DISABLE_MESSAGE"
    }

}