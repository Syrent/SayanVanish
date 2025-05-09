package org.sayandev.sayanvanish.velocity.feature.features.hook

import ch.andre601.advancedserverlist.api.AdvancedServerListAPI
import ch.andre601.advancedserverlist.api.PlaceholderProvider
import ch.andre601.advancedserverlist.api.exceptions.InvalidPlaceholderProviderException
import ch.andre601.advancedserverlist.api.objects.GenericPlayer
import ch.andre601.advancedserverlist.api.objects.GenericServer
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.proxy.config.language
import org.sayandev.sayanvanish.velocity.api.SayanVanishVelocityAPI
import org.sayandev.sayanvanish.velocity.api.SayanVanishVelocityAPI.Companion.user
import org.sayandev.sayanvanish.velocity.feature.HookFeature
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.sayandev.stickynote.velocity.registerListener

@RegisteredFeature
@ConfigSerializable
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
        player: GenericPlayer?,
        server: GenericServer?
    ): String? {
        if (placeholder.equals("vanished", true)) {
            if (player == null) return "false"
            return if (SayanVanishVelocityAPI.getInstance().getVanishedUsers().map { it.username }.contains(player.name)) "true" else "false"
        }

        if (placeholder.equals("level", true)) {
            if (player == null) return "0"
            return player.uuid.user()?.vanishLevel?.toString() ?: "0"
        }

        if (placeholder.equals("count", true)) {
            return SayanVanishVelocityAPI.getInstance().database.getVanishUsers().filter { user -> user.isOnline && user.isVanished }.size.toString()
        }

        if (placeholder.equals("vanish_prefix", true)) {
            return if (player?.uuid?.user()?.isVanished == true) language.vanish.placeholderPrefix else ""
        }

        if (placeholder.equals("vanish_suffix", true)) {
            return if (player?.uuid?.user()?.isVanished == true) language.vanish.placeholderSuffix else ""
        }

        if (placeholder.startsWith("online_")) {
            val type = placeholder.substring(7)
            val vanishedOnlineUsers = SayanVanishVelocityAPI.getInstance().database.getVanishUsers().filter { user -> user.isVanished && user.isOnline }

            return if (type.equals("total", true)) {
                SayanVanishAPI.getDatabase().getBasicUsers(false).filter { !vanishedOnlineUsers.map { vanishUser -> vanishUser.username }.contains(it.username) }.size.toString()
            } else {
                SayanVanishAPI.getDatabase().getBasicUsers(false).filter { it.serverId == type && !vanishedOnlineUsers.map { vanishUser -> vanishUser.username }.contains(it.username) }.size.toString()
            }
        }

        return null
    }

    init {
        registerListener(this)
    }
}