package org.sayandev.sayanvanish.paper.feature.features

import com.charleskorn.kaml.YamlComment
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.sayandev.sayanvanish.api.Permissions
import org.sayandev.sayanvanish.api.feature.Configurable
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.paper.api.SayanVanishPaperAPI.Companion.cachedVanishUser
import org.sayandev.sayanvanish.paper.api.event.PaperUserUnVanishEvent
import org.sayandev.sayanvanish.paper.api.event.PaperUserVanishEvent
import org.sayandev.sayanvanish.paper.feature.ListenedFeature
import org.sayandev.sayanvanish.paper.utils.PlayerUtils.sendRawComponent
import org.sayandev.stickynote.bukkit.hook.PlaceholderAPIHook
import org.sayandev.stickynote.bukkit.onlinePlayers

@RegisteredFeature
@Serializable
@SerialName("fake_message")
class FeatureFakeMessage(
    @YamlComment("Whether to send a fake join message when a player vanishes")
    @Configurable val sendFakeJoinMessage: Boolean = false,
    @YamlComment("The message to send when a player vanishes")
    @Configurable val sendFakeQuitMessage: Boolean = false,
    @YamlComment(
    "The message to send when a player vanishes",
    "",
    "Note: All PlaceholderAPI placeholders are supported",
    "Internal Placeholders:",
    "- <player> - the vanished player's name",
    )
    @Configurable val fakeJoinMessage: String = "<yellow><player> joined the game",
    @YamlComment(
    "The message to send when a player vanishes",
    "",
    "Note: All PlaceholderAPI placeholders are supported",
    "Internal Placeholders:",
    "- <player> - the vanished player's name"
    )
    @Configurable val fakeQuitMessage: String = "<yellow><player> left the game",
    @YamlComment("Whether to use the legacy formatter for the fake messages (NOT RECOMMENDED)")
    @Configurable val useLegacyFormatter: Boolean = false,
    @YamlComment("Whether to disable the join message if the player is vanished")
    @Configurable val disableJoinMessageIfVanished: Boolean = true,
    @YamlComment("Whether to disable the quit message if the player is vanished")
    @Configurable val disableQuitMessageIfVanished: Boolean = true,
) : ListenedFeature() {

    @Transient override val id = "fake_message"
    override var enabled: Boolean = true

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onJoin(event: PlayerJoinEvent) {
        val user = event.player.cachedVanishUser() ?: return
        if (!isActive(user)) return
        if (!user.isVanished && !user.hasPermission(Permissions.VANISH_ON_JOIN)) return
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
        if (!user.isVanished && !user.hasPermission(Permissions.VANISH_ON_JOIN)) return
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
    private fun onVanish(event: PaperUserVanishEvent) {
        val user = event.user
        if (!isActive(user)) return
        if (!event.options.sendMessage) return
        if (sendFakeQuitMessage && !event.options.isOnJoin && !event.options.isOnQuit) {
            for (player in onlinePlayers) {
                if (useLegacyFormatter) {
                    player.sendMessage(
                        LegacyComponentSerializer.legacyAmpersand().deserialize(
                            PlaceholderAPIHook.injectPlaceholders(user.offlinePlayer(), fakeQuitMessage)
                                .replace("<player>", user.username)
                        )
                    )
                } else {
                    player.sendRawComponent(PlaceholderAPIHook.injectPlaceholders(user.offlinePlayer(), fakeQuitMessage), Placeholder.unparsed("player", user.username))
                }
            }
        }
    }

    @EventHandler
    private fun onUnVanish(event: PaperUserUnVanishEvent) {
        val user = event.user
        if (!event.options.sendMessage) return
        if (!isActive(user)) return
        if (sendFakeJoinMessage && !event.options.isOnJoin && !event.options.isOnQuit) {
            for (player in onlinePlayers) {
                if (useLegacyFormatter) {
                    player.sendMessage(
                        LegacyComponentSerializer.legacyAmpersand().deserialize(
                            PlaceholderAPIHook.injectPlaceholders(user.offlinePlayer(), fakeJoinMessage)
                                .replace("<player>", user.username)
                        )
                    )
                } else {
                    player.sendRawComponent(PlaceholderAPIHook.injectPlaceholders(user.offlinePlayer(), fakeJoinMessage), Placeholder.unparsed("player", user.username))
                }
            }
        }
    }
}