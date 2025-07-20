package org.sayandev.sayanvanish.bukkit.feature.features

import org.sayandev.sayanventure.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.sayandev.sayanvanish.api.Permission
import org.sayandev.sayanvanish.api.VanishAPI
import org.sayandev.sayanvanish.api.VanishOptions
import org.sayandev.sayanvanish.api.feature.Configurable
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.cachedVanishUser
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.getCachedOrCreateVanishUser
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.getOrAddUser
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.getOrAddVanishUser
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.user
import org.sayandev.sayanvanish.bukkit.config.language
import org.sayandev.sayanvanish.bukkit.feature.ListenedFeature
import org.sayandev.stickynote.bukkit.launch
import org.sayandev.stickynote.bukkit.utils.AdventureUtils.component
import kotlinx.serialization.Serializable
import com.charleskorn.kaml.YamlComment
import kotlinx.serialization.SerialName

@RegisteredFeature
@Serializable
@SerialName("state")
class FeatureState(
    override var enabled: Boolean = true,
    @YamlComment(
    "This is a CRITICAL feature. It is responsible for handling the state of the player when they join or quit the server.",
    "do NOT disable this feature if you don't know what you're doing.",
    "",
    "If true, players will be remembered when they join the server. (if they were vanished before quitting)",
    )
    @Configurable val remember: Boolean = true,
    @YamlComment("Whether to vanish players when they join the server (they also need vanish on join permission)")
    @Configurable val vanishOnJoin: Boolean = false,
    @YamlComment("Whether to reappear players when they quit the server")
    @Configurable val reappearOnQuit: Boolean = false,
    @YamlComment("Whether to check permission when a player joins the server")
    @Configurable val checkPermissionOnQuit: Boolean = true,
    @YamlComment("Whether to check permission when a player quits the server")
    @Configurable val checkPermissionOnJoin: Boolean = true,
) : ListenedFeature("state", enabled, critical = true) {

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        val user = player.cachedVanishUser()
        if ((user != null && !isActive(user)) || !isActive()) return
        val vanishJoinOptions = VanishOptions.Builder().sendMessage(false).notifyStatusChangeToOthers(false).isOnJoin(true).build()

        if (user == null) {
            if (!player.hasPermission(Permission.VANISH.permission())) {
                return
            }

            val tempUser = player.getCachedOrCreateVanishUser()
            tempUser.isOnline = true

            if (tempUser.hasPermission(Permission.VANISH_ON_JOIN) || vanishOnJoin) {
                tempUser.isVanished = true
                launch {
                    tempUser.disappear(vanishJoinOptions)
                }
            }

            launch {
                tempUser.saveAndSync()
            }
            return
        }

        user.isOnline = true

        if (checkPermissionOnJoin && !user.hasPermission(Permission.VANISH)) {
            user.sendMessage(language.vanish.noPermissionToKeepVanished.component(Placeholder.unparsed("permission", Permission.VANISH.permission())))
            user.appear(vanishJoinOptions)
            launch {
                user.delete()
            }
            return
        }

        if (user.hasPermission(Permission.VANISH_ON_JOIN) || (user.isVanished && remember) || vanishOnJoin) {
            user.isVanished = true
            user.disappear(vanishJoinOptions)
        }

        if (user.isVanished) {
            if (user.currentOptions.notifyJoinQuitVanished) {
                for (vanishedUser in VanishAPI.get().getCacheService().getVanishUsers().values.filter { it.hasPermission(Permission.VANISH) && it.vanishLevel >= user.vanishLevel }) {
                    vanishedUser.sendMessage(language.vanish.joinedTheServerWhileVanished.component(Placeholder.unparsed("player", user.username)))
                }
            }
        }

        launch {
            user.save()
        }

        return
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun updateUserOnQuit(event: PlayerQuitEvent) {
        val player = event.player
        val user = player.cachedVanishUser() ?: return

        if (user.isVanished) {
            if (user.currentOptions.notifyJoinQuitVanished) {
                for (vanishedUser in VanishAPI.get().getCacheService().getVanishUsers().values.filter { it.hasPermission(Permission.VANISH) && it.vanishLevel >= user.vanishLevel }) {
                    vanishedUser.sendMessage(language.vanish.leftTheServerWhileVanished.component(Placeholder.unparsed("player", user.username)))
                }
            }
        }

        if ((reappearOnQuit && user.isVanished) || (checkPermissionOnQuit && !user.hasPermission(Permission.VANISH))) {
            user.appear(VanishOptions.Builder().isOnQuit(true).build())
        }
        user.isOnline = false

        launch {
            user.saveAndSync()
        }
    }

}