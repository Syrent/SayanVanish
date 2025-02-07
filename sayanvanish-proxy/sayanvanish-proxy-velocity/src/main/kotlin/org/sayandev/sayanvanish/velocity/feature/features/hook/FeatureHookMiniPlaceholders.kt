package org.sayandev.sayanvanish.velocity.feature.features.hook

import com.velocitypowered.api.proxy.Player
import io.github.miniplaceholders.api.Expansion
import io.github.miniplaceholders.api.utils.TagsUtils
import org.sayandev.sayanvanish.api.SayanVanishAPI
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.velocity.api.SayanVanishVelocityAPI
import org.sayandev.sayanvanish.velocity.api.SayanVanishVelocityAPI.Companion.user
import org.sayandev.sayanvanish.velocity.feature.HookFeature
import org.sayandev.stickynote.velocity.plugin
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
            val player = audience as? Player ?: return@audiencePlaceholder TagsUtils.EMPTY_TAG
            return@audiencePlaceholder TagsUtils.staticTag(if (player.user()?.isVanished == true) "true" else "false")
        }

        builder.audiencePlaceholder("level") { audience, queue, context ->
            val player = audience as? Player ?: return@audiencePlaceholder TagsUtils.staticTag("0")
            return@audiencePlaceholder TagsUtils.staticTag(player.user()?.vanishLevel?.toString() ?: "0")
        }

        builder.globalPlaceholder("count") { queue, context ->
            TagsUtils.staticTag(SayanVanishVelocityAPI.getInstance().database.getUsers().filter { user -> user.isOnline && user.isVanished }.size.toString())
        }

        for (server in plugin.server.allServers) {
            builder.globalPlaceholder("online_${server.serverInfo.name.lowercase()}") { queue, context ->
                val vanishedOnlineUsers = SayanVanishVelocityAPI.getInstance().database.getUsers().filter { user -> user.isVanished && user.isOnline }
                TagsUtils.staticTag(SayanVanishAPI.getInstance().database.getBasicUsers(false).filter { it.serverId.lowercase() == server.serverInfo.name.lowercase() && !vanishedOnlineUsers.map { vanishUser -> vanishUser.username }.contains(it.username) }.size.toString())
            }
        }

        builder.audiencePlaceholder("online_here") { audience, queue, context ->
            val player = audience as? Player ?: return@audiencePlaceholder TagsUtils.staticTag("0")
            val currentServerVanishedOnlineUsers = SayanVanishVelocityAPI.getInstance().database.getUsers().filter { user -> user.isVanished && user.isOnline && user.serverId == player.currentServer.getOrNull()?.serverInfo?.name }
            TagsUtils.staticTag(SayanVanishAPI.getInstance().database.getBasicUsers(false).filter { !currentServerVanishedOnlineUsers.map { vanishUser -> vanishUser.username }.contains(it.username) }.size.toString())
        }

        builder.globalPlaceholder("online_total") { queue, context ->
            val vanishedOnlineUsers = SayanVanishVelocityAPI.getInstance().database.getUsers().filter { user -> user.isVanished && user.isOnline }
            TagsUtils.staticTag(SayanVanishAPI.getInstance().database.getBasicUsers(false).filter { !vanishedOnlineUsers.map { vanishUser -> vanishUser.username }.contains(it.username) }.size.toString())
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