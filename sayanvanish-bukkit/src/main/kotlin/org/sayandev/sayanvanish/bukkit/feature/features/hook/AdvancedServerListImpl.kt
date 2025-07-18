package org.sayandev.sayanvanish.bukkit.feature.features.hook

import ch.andre601.advancedserverlist.api.AdvancedServerListAPI
import ch.andre601.advancedserverlist.api.PlaceholderProvider
import ch.andre601.advancedserverlist.api.exceptions.InvalidPlaceholderProviderException
import ch.andre601.advancedserverlist.api.objects.GenericPlayer
import ch.andre601.advancedserverlist.api.objects.GenericServer
import org.sayandev.sayanvanish.api.SayanVanishAPI
import org.sayandev.sayanvanish.api.VanishAPI
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.getCachedOrCreateVanishUser
import org.sayandev.sayanvanish.bukkit.config.language
import org.sayandev.stickynote.bukkit.onlinePlayers

// TODO: we have so many placeholder providers, maybe we should create a common base class for them?
class AdvancedServerListImpl : PlaceholderProvider("sayanvanish") {
    fun register() {
        try {
            AdvancedServerListAPI.get()
                .addPlaceholderProvider(this)
        } catch (_: InvalidPlaceholderProviderException) { }
    }

    override fun parsePlaceholder(
        placeholder: String,
        player: GenericPlayer?,
        server: GenericServer?
    ): String? {
        if (placeholder.equals("vanished", true)) {
            if (player == null) return "false"
            return if (player.uuid.getCachedOrCreateVanishUser().isVanished) "true" else "false"
        }

        if (placeholder.equals("level", true)) {
            if (player == null) return "0"
            return player.uuid.getCachedOrCreateVanishUser().vanishLevel.toString()
        }

        if (placeholder.equals("count", true)) {
            return VanishAPI.get().getCacheService().getVanishUsers().getVanishedCount().toString()
        }

        if (placeholder.equals("vanish_prefix", true)) {
            return if (player?.uuid?.getCachedOrCreateVanishUser()?.isVanished == true) language.vanish.placeholderPrefix else ""
        }

        if (placeholder.equals("vanish_suffix", true)) {
            return if (player?.uuid?.getCachedOrCreateVanishUser()?.isVanished == true) language.vanish.placeholderSuffix else ""
        }

        if (placeholder.startsWith("online_")) {
            val type = placeholder.substring(7)
            val vanishedOnlineUsers = VanishAPI.get().getCacheService().getVanishUsers().getOnlineVanished()
            val vanishedOnlineUserNames = vanishedOnlineUsers.map { vanishedOnlineUser -> vanishedOnlineUser.username }

            return if (type.equals("here", true)) {
                onlinePlayers.filter { onlinePlayer -> !vanishedOnlineUserNames.contains(onlinePlayer.name) }.size.toString()
            } else if (type.equals("total", true)) {
                SayanVanishAPI.get().getCacheService().getUsers().values.filter { user -> !vanishedOnlineUserNames.contains(user.username) }.size.toString()
            } else {
                VanishAPI.get().getCacheService().getUsers().getByServer(type).filter { it.isOnline && !vanishedOnlineUserNames.contains(it.username) }.size.toString()
            }
        }

        return null
    }
}