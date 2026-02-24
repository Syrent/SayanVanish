package org.sayandev.sayanvanish.paper.feature.features.hook

import io.github.miniplaceholders.api.Expansion
import io.github.miniplaceholders.api.utils.Tags
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.kyori.adventure.text.minimessage.tag.Tag
import org.bukkit.entity.Player
import org.sayandev.sayanvanish.api.VanishAPI
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.paper.api.SayanVanishBukkitAPI.Companion.cachedVanishUser
import org.sayandev.sayanvanish.paper.config.Settings
import org.sayandev.sayanvanish.paper.config.language
import org.sayandev.sayanvanish.paper.feature.HookFeature
import org.sayandev.stickynote.bukkit.onlinePlayers
import org.sayandev.stickynote.bukkit.utils.AdventureUtils.component

@RegisteredFeature
@Serializable
@SerialName("hook_miniplaceholders")
class FeatureHookMiniPlaceholders: HookFeature() {

    @Transient override val id = "hook_miniplaceholders"
    override var enabled: Boolean = true
    override val plugin: String = "MiniPlaceholders"

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
            return@audiencePlaceholder Tag.selfClosingInserting((if (player.cachedVanishUser()?.isVanished == true) "true" else "false").component())
        }

        builder.audiencePlaceholder("level") { audience, queue, context ->
            val player = audience as? Player ?: return@audiencePlaceholder Tag.selfClosingInserting("0".component())
            return@audiencePlaceholder Tag.selfClosingInserting((player.cachedVanishUser()?.vanishLevel?.toString() ?: "0").component())
        }

        builder.globalPlaceholder("count") { queue, context ->
            Tag.selfClosingInserting(VanishAPI.get().getCacheService().getVanishUsers().values.filter { it.isVanished && it.isOnline }.size.toString().component())
        }

        builder.audiencePlaceholder("vanish_prefix") { audience, queue, context ->
            Tag.selfClosingInserting((if ((audience as? Player)?.cachedVanishUser()?.isVanished == true) language.vanish.placeholderPrefix else "").component())
        }

        builder.audiencePlaceholder("vanish_suffix") { audience, queue, context ->
            Tag.selfClosingInserting((if ((audience as? Player)?.cachedVanishUser()?.isVanished == true) language.vanish.placeholderSuffix else "").component())
        }

        builder.globalPlaceholder("online") { queue, context ->
            if (!queue.hasNext()) {
                return@globalPlaceholder Tags.EMPTY_TAG
            }

            val vanishedOnlineUsers = VanishAPI.get().getCacheService().getVanishUsers().values.filter { user -> user.isVanished && user.isOnline }
            val serverName = queue.pop().value()

            val result = when (serverName) {
                "here" -> {
                    onlinePlayers.filter { onlinePlayer -> !vanishedOnlineUsers.map { vanishedOnlineUser -> vanishedOnlineUser.username }.contains(onlinePlayer.name) }.size.toString()
                }
                "total" -> {
                    if (!Settings.get().general.proxyMode) {
                        "PROXY_MODE IS NOT ENABLED!"
                    } else {
                        VanishAPI.get().getCacheService().getUsers().values.filter { !vanishedOnlineUsers.map { vanishUser -> vanishUser.username }.contains(it.username) }.size.toString()
                    }
                }
                else -> {
                    if (!Settings.get().general.proxyMode) {
                        "PROXY_MODE IS NOT ENABLED!"
                    } else {
                        VanishAPI.get().getCacheService().getUsers().values.filter { it.serverId == serverName && !vanishedOnlineUsers.map { vanishUser -> vanishUser.username }.contains(it.username) }.size.toString()
                    }
                }
            }

            Tag.selfClosingInserting(result.component())
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