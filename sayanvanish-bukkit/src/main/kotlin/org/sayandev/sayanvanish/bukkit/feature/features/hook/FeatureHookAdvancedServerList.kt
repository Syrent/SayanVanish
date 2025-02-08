package org.sayandev.sayanvanish.bukkit.feature.features.hook

import ch.andre601.advancedserverlist.api.AdvancedServerListAPI
import ch.andre601.advancedserverlist.api.PlaceholderProvider
import ch.andre601.advancedserverlist.api.exceptions.InvalidPlaceholderProviderException
import ch.andre601.advancedserverlist.api.objects.GenericPlayer
import ch.andre601.advancedserverlist.api.objects.GenericServer
import org.sayandev.sayanvanish.api.SayanVanishAPI
import org.sayandev.sayanvanish.api.SayanVanishAPI.Companion.user
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI
import org.sayandev.sayanvanish.bukkit.config.language
import org.sayandev.sayanvanish.bukkit.config.settings
import org.sayandev.sayanvanish.bukkit.feature.HookFeature
import org.sayandev.stickynote.bukkit.onlinePlayers
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@RegisteredFeature
@ConfigSerializable
class FeatureHookAdvancedServerList : HookFeature("hook_advanced_server_list", "AdvancedServerList") {
    override fun enable() {
        if (hasPlugin()) {
            try {
                AdvancedServerListAPI.get()
                    .addPlaceholderProvider(AdvancedServerListImpl())
            } catch (_: InvalidPlaceholderProviderException) { }
        }
        super.enable()
    }
}

private class AdvancedServerListImpl : PlaceholderProvider("sayanvanish") {
    override fun parsePlaceholder(
        placeholder: String,
        player: GenericPlayer?,
        server: GenericServer?
    ): String? {
        if (placeholder.equals("vanished", true)) {
            if (player == null) return "false"
            return if (SayanVanishBukkitAPI.getInstance().getVanishedUsers().map { it.username }.contains(player.name)) "true" else "false"
        }

        if (placeholder.equals("level", true)) {
            if (player == null) return "0"
            return SayanVanishBukkitAPI.getInstance().getUser(player.uuid)?.vanishLevel?.toString() ?: "0"
        }

        if (placeholder.equals("count", true)) {
            return SayanVanishBukkitAPI.getInstance().database.getUsers().filter { user -> user.isOnline && user.isVanished }.size.toString()
        }

        if (placeholder.equals("vanish_prefix", true)) {
            return if (player?.uuid?.user()?.isVanished == true) language.vanish.placeholderPrefix else ""
        }

        if (placeholder.equals("vanish_suffix", true)) {
            return if (player?.uuid?.user()?.isVanished == true) language.vanish.placeholderSuffix else ""
        }

        if (placeholder.startsWith("online_")) {
            val type = placeholder.substring(7)
            val vanishedOnlineUsers = SayanVanishBukkitAPI.getInstance().database.getUsers().filter { user -> user.isVanished && user.isOnline }

            return if (type.equals("here", true)) {
                onlinePlayers.filter { onlinePlayer -> !vanishedOnlineUsers.map { vanishedOnlineUser -> vanishedOnlineUser.username }.contains(onlinePlayer.name) }.size.toString()
            } else if (type.equals("total", true)) {
                if (!settings.general.proxyMode) {
                    return "PROXY_MODE IS NOT ENABLED!"
                }
                SayanVanishAPI.getInstance().database.getBasicUsers(false).filter { !vanishedOnlineUsers.map { vanishUser -> vanishUser.username }.contains(it.username) }.size.toString()
            } else {
                if (!settings.general.proxyMode) {
                    return "PROXY_MODE IS NOT ENABLED!"
                }
                SayanVanishAPI.getInstance().database.getBasicUsers(false).filter { it.serverId == type && !vanishedOnlineUsers.map { vanishUser -> vanishUser.username }.contains(it.username) }.size.toString()
            }
        }

        return null
    }
}