package org.sayandev.sayanvanish.bukkit.feature.features.hook

import io.github.miniplaceholders.api.Expansion
import io.github.miniplaceholders.api.utils.Tags
import net.kyori.adventure.text.minimessage.tag.Tag
import org.bukkit.entity.Player
import org.sayandev.sayanvanish.api.SayanVanishAPI
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.user
import org.sayandev.sayanvanish.bukkit.config.language
import org.sayandev.sayanvanish.bukkit.config.settings
import org.sayandev.sayanvanish.bukkit.feature.HookFeature
import org.sayandev.stickynote.bukkit.onlinePlayers
import org.sayandev.stickynote.bukkit.utils.AdventureUtils.adventureComponent
import org.sayandev.stickynote.bukkit.warn
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
            val player = audience as? Player ?: return@audiencePlaceholder Tags.EMPTY_TAG
            return@audiencePlaceholder Tag.selfClosingInserting((if (player.user()?.isVanished == true) "true" else "false").adventureComponent())
        }

        builder.audiencePlaceholder("level") { audience, queue, context ->
            val player = audience as? Player ?: return@audiencePlaceholder Tag.selfClosingInserting("0".adventureComponent())
            return@audiencePlaceholder Tag.selfClosingInserting((player.user()?.vanishLevel?.toString() ?: "0").adventureComponent())
        }

        builder.globalPlaceholder("count") { queue, context ->
            Tag.selfClosingInserting(SayanVanishBukkitAPI.getInstance().database.getUsers().filter { user -> user.isOnline && user.isVanished }.size.toString().adventureComponent())
        }

        builder.audiencePlaceholder("vanish_prefix") { audience, queue, context ->
            Tag.selfClosingInserting((if ((audience as? Player)?.user()?.isVanished == true) language.vanish.placeholderPrefix else "").adventureComponent())
        }

        builder.audiencePlaceholder("vanish_suffix") { audience, queue, context ->
            Tag.selfClosingInserting((if ((audience as? Player)?.user()?.isVanished == true) language.vanish.placeholderSuffix else "").adventureComponent())
        }

        builder.globalPlaceholder("online") { queue, context ->
            if (!queue.hasNext()) {
                return@globalPlaceholder Tags.EMPTY_TAG
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

            Tag.selfClosingInserting(result.adventureComponent())
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