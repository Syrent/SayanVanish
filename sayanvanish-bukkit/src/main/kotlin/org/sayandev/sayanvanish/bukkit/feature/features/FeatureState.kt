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
import org.spongepowered.configurate.objectmapping.meta.Comment

@RegisteredFeature
@ConfigSerializable
data class FeatureState(
    override var enabled: Boolean = true,
    @Comment("""
    This is a CRITICAL feature. It is responsible for handling the state of the player when they join or quit the server.
    do NOT disable this feature if you don't know what you're doing.
    
    If true, players will be remembered when they join the server. (if they were vanished before quitting)
    """)
    @Configurable val remember: Boolean = true,
    @Comment("Whether to vanish players when they join the server (they also need vanish on join permission)")
    @Configurable val vanishOnJoin: Boolean = false,
    @Comment("Whether to reappear players when they quit the server")
    @Configurable val reappearOnQuit: Boolean = false,
    @Comment("Whether to check permission when a player joins the server")
    @Configurable val checkPermissionOnQuit: Boolean = true,
    @Comment("Whether to check permission when a player quits the server")
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