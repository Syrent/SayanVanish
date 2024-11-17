package org.sayandev.sayanvanish.bukkit.feature.features

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
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
import org.sayandev.sayanvanish.bukkit.utils.PlayerUtils.sendComponent
import org.sayandev.stickynote.bukkit.onlinePlayers
import org.sayandev.stickynote.bukkit.utils.AdventureUtils.sendComponent
import org.spongepowered.configurate.objectmapping.ConfigSerializable

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
        val user = event.player.user() ?: return
        if (!isActive(user)) return
        if (!user.isVanished && !user.hasPermission(Permission.VANISH_ON_JOIN)) return
        if (disableJoinMessageIfVanished) {
            event.joinMessage = null
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onQuit(event: PlayerQuitEvent) {
        val user = event.player.user() ?: return
        if (!isActive(user)) return
        if (!user.isVanished) return
        if (disableQuitMessageIfVanished) {
            event.quitMessage = null
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onJoinLast(event: PlayerJoinEvent) {
        val user = event.player.user() ?: return
        if (!isActive(user)) return
        if (!user.isVanished && !user.hasPermission(Permission.VANISH_ON_JOIN)) return
        if (disableJoinMessageIfVanished) {
            event.joinMessage = null
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onQuitLast(event: PlayerQuitEvent) {
        val user = event.player.user() ?: return
        if (!isActive(user)) return
        if (!user.isVanished) return
        if (disableQuitMessageIfVanished) {
            event.quitMessage = null
        }
    }

    @EventHandler
    private fun onVanish(event: BukkitUserVanishEvent) {
        val user = event.user
        if (!isActive(user)) return
        if (!event.options.sendMessage) return
        if (sendFakeQuitMessage && !event.options.isOnJoin && !event.options.isOnQuit) {
            for (player in onlinePlayers) {
                player.sendComponent(fakeQuitMessage, Placeholder.unparsed("player", user.username))
            }
        }
    }

    @EventHandler
    private fun onUnVanish(event: BukkitUserUnVanishEvent) {
        if (!event.options.sendMessage) return
        if (!isActive(event.user)) return
        if (sendFakeJoinMessage && !event.options.isOnJoin && !event.options.isOnQuit) {
            for (player in onlinePlayers) {
                player.sendComponent(fakeJoinMessage, Placeholder.unparsed("player", event.user.username))
            }
        }
    }

}