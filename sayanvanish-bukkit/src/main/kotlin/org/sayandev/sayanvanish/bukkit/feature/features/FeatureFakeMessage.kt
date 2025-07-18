package org.sayandev.sayanvanish.bukkit.feature.features

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.sayandev.sayanvanish.api.Permission
import org.sayandev.sayanvanish.api.feature.Configurable
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.cachedVanishUser
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.user
import org.sayandev.sayanvanish.bukkit.api.event.BukkitUserUnVanishEvent
import org.sayandev.sayanvanish.bukkit.api.event.BukkitUserVanishEvent
import org.sayandev.sayanvanish.bukkit.feature.ListenedFeature
import org.sayandev.sayanvanish.bukkit.utils.PlayerUtils.sendComponent
import org.sayandev.sayanvanish.bukkit.utils.PlayerUtils.sendRawComponent
import org.sayandev.stickynote.bukkit.hook.PlaceholderAPIHook
import org.sayandev.stickynote.bukkit.onlinePlayers
import org.sayandev.stickynote.bukkit.utils.AdventureUtils
import org.sayandev.stickynote.bukkit.utils.AdventureUtils.sendComponent
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment

@RegisteredFeature
@ConfigSerializable
class FeatureFakeMessage(
    @Comment("Whether to send a fake join message when a player vanishes")
    @Configurable val sendFakeJoinMessage: Boolean = false,
    @Comment("The message to send when a player vanishes")
    @Configurable val sendFakeQuitMessage: Boolean = false,
    @Comment("""
    The message to send when a player vanishes
    
    Note: All PlaceholderAPI placeholders are supported
    Internal Placeholders:
    - <player> - the vanished player's name
    """)
    @Configurable val fakeJoinMessage: String = "<yellow><player> joined the game",
    @Comment("""
    The message to send when a player vanishes
    
    Note: All PlaceholderAPI placeholders are supported
    Internal Placeholders:
    - <player> - the vanished player's name
    """)
    @Configurable val fakeQuitMessage: String = "<yellow><player> left the game",
    @Comment("Whether to use the legacy formatter for the fake messages (NOT RECOMMENDED)")
    @Configurable val useLegacyFormatter: Boolean = false,
    @Comment("Whether to disable the join message if the player is vanished")
    @Configurable val disableJoinMessageIfVanished: Boolean = true,
    @Comment("Whether to disable the quit message if the player is vanished")
    @Configurable val disableQuitMessageIfVanished: Boolean = true,
) : ListenedFeature("fake_message") {

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onJoin(event: PlayerJoinEvent) {
        val user = event.player.cachedVanishUser() ?: return
        if (!isActive(user)) return
        if (!user.isVanished && !user.hasPermission(Permission.VANISH_ON_JOIN)) return
        if (disableJoinMessageIfVanished) {
            event.joinMessage = null
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onQuit(event: PlayerQuitEvent) {
        val user = event.player.cachedVanishUser() ?: return
        if (!isActive(user)) return
        if (!user.isVanished) return
        if (disableQuitMessageIfVanished) {
            event.quitMessage = null
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onJoinLast(event: PlayerJoinEvent) {
        val user = event.player.cachedVanishUser() ?: return
        if (!isActive(user)) return
        if (!user.isVanished && !user.hasPermission(Permission.VANISH_ON_JOIN)) return
        if (disableJoinMessageIfVanished) {
            event.joinMessage = null
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onQuitLast(event: PlayerQuitEvent) {
        val user = event.player.cachedVanishUser() ?: return
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
                if (useLegacyFormatter) {
                    AdventureUtils.audience.player(player).sendMessage(
                        LegacyComponentSerializer.legacyAmpersand().deserialize(
                            PlaceholderAPIHook.injectPlaceholders(user.offlinePlayer(), fakeQuitMessage)
                                .replace("<player>", user.username)
                        )
                    )
                } else {
                    player.sendRawComponent(fakeQuitMessage, Placeholder.unparsed("player", user.username))
                }
            }
        }
    }

    @EventHandler
    private fun onUnVanish(event: BukkitUserUnVanishEvent) {
        val user = event.user
        if (!event.options.sendMessage) return
        if (!isActive(user)) return
        if (sendFakeJoinMessage && !event.options.isOnJoin && !event.options.isOnQuit) {
            for (player in onlinePlayers) {
                if (useLegacyFormatter) {
                    AdventureUtils.audience.player(player).sendMessage(
                        LegacyComponentSerializer.legacyAmpersand().deserialize(
                            PlaceholderAPIHook.injectPlaceholders(user.offlinePlayer(), fakeJoinMessage)
                                .replace("<player>", user.username)
                        )
                    )
                } else {
                    player.sendRawComponent(fakeJoinMessage, Placeholder.unparsed("player", user.username))
                }
            }
        }
    }
}