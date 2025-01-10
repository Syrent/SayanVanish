package org.sayandev.sayanvanish.bukkit.feature.features

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.sayandev.sayanvanish.api.Permission
import org.sayandev.sayanvanish.api.VanishOptions
import org.sayandev.sayanvanish.api.feature.Configurable
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.getOrCreateUser
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.user
import org.sayandev.sayanvanish.bukkit.config.language
import org.sayandev.sayanvanish.bukkit.feature.ListenedFeature
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@RegisteredFeature
@ConfigSerializable
class FeatureState(
    @Configurable val remember: Boolean = true,
    @Configurable val vanishOnJoin: Boolean = false,
    @Configurable val reappearOnQuit: Boolean = false,
    @Configurable val checkPermissionOnQuit: Boolean = true,
    @Configurable val checkPermissionOnJoin: Boolean = true,
) : ListenedFeature("state", critical = true) {

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        val user = player.user()
        if ((user != null && !isActive(user)) || !isActive()) return
        val vanishJoinOptions = VanishOptions.Builder().sendMessage(false).notifyStatusChangeToOthers(false).isOnJoin(true).build()

        if (user == null) {
            if (!player.hasPermission(Permission.VANISH.permission())) {
                return
            }

            val tempUser = player.getOrCreateUser()

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
            user.sendComponent(language.vanish.noPermissionToKeepVanished, Placeholder.unparsed("permission", Permission.VANISH.permission()))
            user.unVanish(vanishJoinOptions)
            user.delete()
            return
        }

        if (user.hasPermission(Permission.VANISH_ON_JOIN) || (user.isVanished && remember) || vanishOnJoin) {
            user.isVanished = true
            user.vanish(vanishJoinOptions)
        }

        if (user.isVanished) {
            if (user.currentOptions.notifyJoinQuitVanished) {
                for (vanishedUser in SayanVanishBukkitAPI.getInstance().database.getUsers().filter { it.hasPermission(Permission.VANISH) && it.vanishLevel >= user.vanishLevel }) {
                    vanishedUser.sendComponent(language.vanish.joinedTheServerWhileVanished, Placeholder.unparsed("player", user.username))
                }
            }
        }

        user.save()
        return
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun updateUserOnQuit(event: PlayerQuitEvent) {
        val player = event.player
        val user = player.user() ?: return

        if (user.isVanished) {
            if (user.currentOptions.notifyJoinQuitVanished) {
                for (vanishedUser in SayanVanishBukkitAPI.getInstance().database.getUsers().filter { it.hasPermission(Permission.VANISH) && it.vanishLevel >= user.vanishLevel }) {
                    vanishedUser.sendComponent(language.vanish.leftTheServerWhileVanished, Placeholder.unparsed("player", user.username))
                }
            }
        }

        if ((reappearOnQuit && user.isVanished) || (checkPermissionOnQuit && !user.hasPermission(Permission.VANISH))) {
            user.unVanish(VanishOptions.Builder().isOnQuit(true).build())
        }
        user.isOnline = false

        user.save()
    }

}