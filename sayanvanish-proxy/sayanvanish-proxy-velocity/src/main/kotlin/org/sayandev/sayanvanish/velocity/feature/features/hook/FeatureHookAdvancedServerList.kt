package org.sayandev.sayanvanish.velocity.feature.features.hook

import ch.andre601.advancedserverlist.api.AdvancedServerListAPI
import ch.andre601.advancedserverlist.api.PlaceholderProvider
import ch.andre601.advancedserverlist.api.exceptions.InvalidPlaceholderProviderException
import ch.andre601.advancedserverlist.api.objects.GenericPlayer
import ch.andre601.advancedserverlist.api.objects.GenericServer
import org.sayandev.sayanvanish.api.VanishAPI
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.proxy.config.language
import org.sayandev.sayanvanish.velocity.feature.HookFeature
import kotlinx.serialization.Serializable
import org.sayandev.stickynote.velocity.registerListener

@RegisteredFeature
@Serializable
class FeatureHookAdvancedServerList : HookFeature("hook_advanced_server_list", "advancedserverlist") {
    override fun enable() {
        if (hasPlugin()) {
            AdvancedServerListImpl()
        }
        super.enable()
    }
}

private class AdvancedServerListImpl : PlaceholderProvider("sayanvanish") {
    init {
        try {
            AdvancedServerListAPI.get()
                .addPlaceholderProvider(this)
        } catch (_: InvalidPlaceholderProviderException) { }
    }

    override fun parsePlaceholder(
        placeholder: String,
        player: GenericPlayer,
        server: GenericServer
    ): String? {
        if (placeholder.equals("vanished", true)) {
            return if (VanishAPI.get().getDatabase().getVanishUserCache(player.uuid)?.isVanished == true) "true" else "false"
        }

        if (placeholder.equals("level", true)) {
            return VanishAPI.get().getDatabase().getVanishUserCache(player.uuid)?.vanishLevel?.toString() ?: "0"
        }

        if (placeholder.equals("count", true)) {
            return VanishAPI.get().getDatabase().getCachedVanishUsers().values.filter { user -> user.isOnline && user.isVanished }.size.toString()
        }

        if (placeholder.equals("vanish_prefix", true)) {
            return if (VanishAPI.get().getDatabase().getVanishUserCache(player.uuid)?.isVanished == true) language.vanish.placeholderPrefix else ""
        }

        if (placeholder.equals("vanish_suffix", true)) {
            return if (VanishAPI.get().getDatabase().getVanishUserCache(player.uuid)?.isVanished == true) language.vanish.placeholderSuffix else ""
        }

        if (placeholder.startsWith("online_")) {
            val type = placeholder.substring(7)
            val vanishedOnlineUsers = VanishAPI.get().getDatabase().getCachedVanishUsers().values.filter { user -> user.isVanished && user.isOnline }

            return if (type.equals("total", true)) {
                VanishAPI.get().getDatabase().getCachedUserCount().totalCount().minus(vanishedOnlineUsers.size).toString()
            } else {
                VanishAPI.get().getDatabase().getCachedUserCount()[type.lowercase()].toString()
            }
        }

        return null
    }

    init {
        registerListener(this)
    }
}