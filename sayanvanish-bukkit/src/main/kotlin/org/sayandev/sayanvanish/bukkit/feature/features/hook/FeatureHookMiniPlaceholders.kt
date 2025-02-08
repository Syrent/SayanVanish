package org.sayandev.sayanvanish.bukkit.feature.features.hook

import io.github.miniplaceholders.api.Expansion
import io.github.miniplaceholders.api.utils.TagsUtils
import org.bukkit.entity.Player
import org.sayandev.sayanvanish.api.SayanVanishAPI
import org.sayandev.sayanvanish.api.SayanVanishAPI.Companion.user
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.user
import org.sayandev.sayanvanish.bukkit.config.language
import org.sayandev.sayanvanish.bukkit.config.settings
import org.sayandev.sayanvanish.bukkit.feature.HookFeature
import org.sayandev.stickynote.bukkit.onlinePlayers
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@RegisteredFeature
@ConfigSerializable
class FeatureHookMiniPlaceholders: HookFeature("hook_miniplaceholders", "MiniPlaceholders") {

    override fun enable() {
        if (hasPlugin()) {
            MiniPlaceholdersHookImpl(this).register()
        }
        super.enable()
    }

    override fun disable(reload: Boolean) {
        if (hasPlugin()) {
            MiniPlaceholdersHookImpl(this).unregister()
        }
        super.disable(reload)
    }

}

private class MiniPlaceholdersHookImpl(val feature: FeatureHookMiniPlaceholders) {
    val builder = Expansion.builder("sayanvanish")

    fun register() {
        unregister()

        builder.audiencePlaceholder("vanished") { audience, queue, context ->
            val player = audience as? Player ?: return@audiencePlaceholder TagsUtils.EMPTY_TAG
            return@audiencePlaceholder TagsUtils.staticTag(if (player.user()?.isVanished == true) "true" else "false")
        }

        builder.audiencePlaceholder("level") { audience, queue, context ->
            val player = audience as? Player ?: return@audiencePlaceholder TagsUtils.staticTag("0")
            return@audiencePlaceholder TagsUtils.staticTag(player.user()?.vanishLevel?.toString() ?: "0")
        }

        builder.globalPlaceholder("count") { queue, context ->
            TagsUtils.staticTag(SayanVanishBukkitAPI.getInstance().database.getUsers().filter { user -> user.isOnline && user.isVanished }.size.toString())
        }

        builder.audiencePlaceholder("vanish_prefix") { audience, queue, context ->
            TagsUtils.staticTag(if ((audience as? Player)?.user()?.isVanished == true) language.vanish.placeholderPrefix else "")
        }

        builder.audiencePlaceholder("vanish_suffix") { audience, queue, context ->
            TagsUtils.staticTag(if ((audience as? Player)?.user()?.isVanished == true) language.vanish.placeholderSuffix else "")
        }

        builder.globalPlaceholder("online") { queue, context ->
            if (!queue.hasNext()) {
                return@globalPlaceholder TagsUtils.EMPTY_TAG
            }

            val vanishedOnlineUsers = SayanVanishBukkitAPI.getInstance().database.getUsers().filter { user -> user.isVanished && user.isOnline }
            val serverName = queue.pop().value()

            val result = when (serverName) {
                "here" -> {
                    onlinePlayers.filter { onlinePlayer -> !vanishedOnlineUsers.map { vanishedOnlineUser -> vanishedOnlineUser.username }.contains(onlinePlayer.name) }.size.toString()
                }
                "total" -> {
                    if (!settings.general.proxyMode) {
                        "PROXY_MODE IS NOT ENABLED!"
                    } else {
                        SayanVanishAPI.getInstance().database.getBasicUsers(false).filter { !vanishedOnlineUsers.map { vanishUser -> vanishUser.username }.contains(it.username) }.size.toString()
                    }
                }
                else -> {
                    if (!settings.general.proxyMode) {
                        "PROXY_MODE IS NOT ENABLED!"
                    } else {
                        SayanVanishAPI.getInstance().database.getBasicUsers(false).filter { it.serverId == serverName && !vanishedOnlineUsers.map { vanishUser -> vanishUser.username }.contains(it.username) }.size.toString()
                    }
                }
            }

            TagsUtils.staticTag(result)
        }

        builder.build().register()
    }

    fun unregister() {
        val expansion = builder.build()
        if (expansion.registered()) {
            expansion.unregister()
        }
    }
}