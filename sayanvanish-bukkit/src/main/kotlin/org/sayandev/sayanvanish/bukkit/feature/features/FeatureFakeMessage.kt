package org.sayandev.sayanvanish.bukkit.feature.features

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.sayandev.sayanvanish.api.Permission
import org.sayandev.sayanvanish.api.feature.Configurable
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.user
import org.sayandev.sayanvanish.bukkit.api.event.BukkitUserUnVanishEvent
import org.sayandev.sayanvanish.bukkit.api.event.BukkitUserVanishEvent
import org.sayandev.sayanvanish.bukkit.feature.ListenedFeature
import org.sayandev.stickynote.bukkit.onlinePlayers
import org.sayandev.stickynote.bukkit.utils.AdventureUtils.component
import org.sayandev.stickynote.bukkit.utils.AdventureUtils.sendMessage
import org.sayandev.stickynote.bukkit.warn
import org.sayandev.stickynote.lib.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.sayandev.stickynote.lib.spongepowered.configurate.objectmapping.ConfigSerializable

@RegisteredFeature
@ConfigSerializable
class FeatureFakeMessage(
    @Configurable val sendFakeJoinMessage: Boolean = false,
    @Configurable val sendFakeQuitMessage: Boolean = false,
    @Configurable val fakeJoinMessage: String = "<yellow><player> joined the game",
    @Configurable val fakeQuitMessage: String = "<yellow><player> left the game",
    @Configurable val disableJoinMessageIfVanished: Boolean = true,
    @Configurable val disableQuitMessageIfVanished: Boolean = true,
) : ListenedFeature("fake_message") {

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onJoin(event: PlayerJoinEvent) {
        if (!isActive()) return
        val user = event.player.user(false) ?: return
        if (!user.isVanished || user.hasPermission(Permission.VANISH_ON_JOIN)) return
        if (disableJoinMessageIfVanished) {
            event.joinMessage = null
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onQuit(event: PlayerQuitEvent) {
        if (!isActive()) return
        val user = event.player.user() ?: return
        if (!user.isVanished) return
        if (disableQuitMessageIfVanished) {
            event.quitMessage = null
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onJoinLast(event: PlayerJoinEvent) {
        if (!isActive()) return
        val user = event.player.user(false) ?: return
        if (!user.isVanished || user.hasPermission(Permission.VANISH_ON_JOIN)) return
        if (disableJoinMessageIfVanished) {
            event.joinMessage = null
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onQuitLast(event: PlayerQuitEvent) {
        if (!isActive()) return
        val user = event.player.user() ?: return
        if (!user.isVanished) return
        if (disableQuitMessageIfVanished) {
            event.quitMessage = null
        }
    }

    @EventHandler
    private fun onVanish(event: BukkitUserVanishEvent) {
        if (!isActive()) return
        if (sendFakeQuitMessage && !event.options.isOnJoin) {
            for (player in onlinePlayers) {
                player.sendMessage(fakeQuitMessage.component(Placeholder.unparsed("player", event.user.username)))
            }
        }
    }

    @EventHandler
    private fun onUnVanish(event: BukkitUserUnVanishEvent) {
        if (!isActive()) return
        if (sendFakeJoinMessage && !event.options.isOnJoin) {
            for (player in onlinePlayers) {
                player.sendMessage(fakeJoinMessage.component(Placeholder.unparsed("player", event.user.username)))
            }
        }
    }

}