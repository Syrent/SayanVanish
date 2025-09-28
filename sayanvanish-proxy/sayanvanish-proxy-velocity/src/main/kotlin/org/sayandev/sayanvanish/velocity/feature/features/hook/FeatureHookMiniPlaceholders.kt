package org.sayandev.sayanvanish.velocity.feature.features.hook

import com.velocitypowered.api.proxy.Player
import io.github.miniplaceholders.api.Expansion
import io.github.miniplaceholders.api.utils.Tags
import net.kyori.adventure.text.minimessage.tag.Tag
import org.sayandev.sayanvanish.api.SayanVanishAPI
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.proxy.config.language
import org.sayandev.sayanvanish.velocity.api.SayanVanishVelocityAPI
import org.sayandev.sayanvanish.velocity.api.SayanVanishVelocityAPI.Companion.user
import org.sayandev.sayanvanish.velocity.feature.HookFeature
import org.sayandev.stickynote.velocity.plugin
import org.sayandev.stickynote.velocity.utils.AdventureUtils.component
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import kotlin.jvm.optionals.getOrNull

@RegisteredFeature
@ConfigSerializable
class FeatureHookMiniPlaceholders: HookFeature("hook_miniplaceholders", "miniplaceholders") {

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
            return@audiencePlaceholder Tag.selfClosingInserting((if (player.user()?.isVanished == true) "true" else "false").component())
        }

        builder.audiencePlaceholder("level") { audience, queue, context ->
            val player = audience as? Player ?: return@audiencePlaceholder Tag.selfClosingInserting("0".component())
            return@audiencePlaceholder Tag.selfClosingInserting((player.user()?.vanishLevel?.toString() ?: "0").component())
        }

        builder.globalPlaceholder("count") { queue, context ->
            Tag.selfClosingInserting(SayanVanishVelocityAPI.getInstance().database.getUsers().filter { user -> user.isOnline && user.isVanished }.size.toString().component())
        }

        builder.audiencePlaceholder("vanish_prefix") { audience, queue, context ->
            Tag.selfClosingInserting((if ((audience as? Player)?.user()?.isVanished == true) language.vanish.placeholderPrefix else "").component())
        }

        builder.audiencePlaceholder("vanish_suffix") { audience, queue, context ->
            Tag.selfClosingInserting((if ((audience as? Player)?.user()?.isVanished == true) language.vanish.placeholderSuffix else "").component())
        }

        for (server in plugin.server.allServers) {
            builder.globalPlaceholder("online_${server.serverInfo.name.lowercase()}") { queue, context ->
                val vanishedOnlineUsers = SayanVanishVelocityAPI.getInstance().database.getUsers().filter { user -> user.isVanished && user.isOnline }
                Tag.selfClosingInserting(SayanVanishAPI.getInstance().database.getBasicUsers(false).filter { it.serverId.lowercase() == server.serverInfo.name.lowercase() && !vanishedOnlineUsers.map { vanishUser -> vanishUser.username }.contains(it.username) }.size.toString().component())
            }
        }

        builder.audiencePlaceholder("online_here") { audience, queue, context ->
            val player = audience as? Player ?: return@audiencePlaceholder Tag.selfClosingInserting("0".component())
            val currentServerVanishedOnlineUsers = SayanVanishVelocityAPI.getInstance().database.getUsers().filter { user -> user.isVanished && user.isOnline && user.serverId == player.currentServer.getOrNull()?.serverInfo?.name }
            Tag.selfClosingInserting(SayanVanishAPI.getInstance().database.getBasicUsers(false).filter { !currentServerVanishedOnlineUsers.map { vanishUser -> vanishUser.username }.contains(it.username) }.size.toString().component())
        }

        builder.globalPlaceholder("online_total") { queue, context ->
            val vanishedOnlineUsers = SayanVanishVelocityAPI.getInstance().database.getUsers().filter { user -> user.isVanished && user.isOnline }
            Tag.selfClosingInserting(SayanVanishAPI.getInstance().database.getBasicUsers(false).filter { !vanishedOnlineUsers.map { vanishUser -> vanishUser.username }.contains(it.username) }.size.toString().component())
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