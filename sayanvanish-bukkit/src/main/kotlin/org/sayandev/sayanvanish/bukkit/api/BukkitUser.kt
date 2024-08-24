package org.sayandev.sayanvanish.bukkit.api

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue
import org.sayandev.sayanvanish.api.Permission
import org.sayandev.sayanvanish.api.User
import org.sayandev.sayanvanish.api.VanishOptions
import org.sayandev.sayanvanish.api.feature.Features
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.getOrCreateUser
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.user
import org.sayandev.sayanvanish.bukkit.api.event.BukkitUserUnVanishEvent
import org.sayandev.sayanvanish.bukkit.api.event.BukkitUserVanishEvent
import org.sayandev.sayanvanish.bukkit.config.language
import org.sayandev.sayanvanish.bukkit.config.settings
import org.sayandev.sayanvanish.bukkit.feature.features.FeatureLevel
import org.sayandev.sayanvanish.bukkit.feature.features.hook.FeatureLuckPermsHook
import org.sayandev.stickynote.bukkit.extension.sendComponent
import org.sayandev.stickynote.bukkit.extension.sendComponentActionbar
import org.sayandev.stickynote.bukkit.onlinePlayers
import org.sayandev.stickynote.bukkit.plugin
import org.sayandev.stickynote.bukkit.server
import org.sayandev.stickynote.bukkit.utils.ServerVersion
import java.util.*

open class BukkitUser(
    override val uniqueId: UUID,
    override var username: String
) : User {

    override var serverId = settings.general.serverId
    override var currentOptions = VanishOptions.defaultOptions()
    override var isVanished = false
    override var isOnline: Boolean = Bukkit.getPlayer(uniqueId) != null
    override var vanishLevel: Int = 1
        get() = if (Features.getFeature<FeatureLevel>().levelMethod == FeatureLevel.LevelMethod.PERMISSION) {
            player()?.let { player ->
                player.effectivePermissions
                    .filter { it.permission.startsWith("sayanvanish.level.") }
                    .maxOfOrNull { it.permission.substringAfter("sayanvanish.level.").toIntOrNull() ?: field } ?: field
            } ?: field
        } else {
            field
        }


    fun stateText(isVanished: Boolean = this.isVanished) = if (isVanished) "<green>ON</green>" else "<red>OFF</red>"

    fun player(): Player? = Bukkit.getPlayer(uniqueId)
    fun offlinePlayer(): OfflinePlayer = Bukkit.getOfflinePlayer(uniqueId)

    override fun vanish(options: VanishOptions) {
        val vanishEvent = BukkitUserVanishEvent(this, options)
        server.pluginManager.callEvent(vanishEvent)
        if (vanishEvent.isCancelled) return
        val options = vanishEvent.options
        currentOptions = options

        if (ServerVersion.supports(9)) {
            player()?.isCollidable = false
        }
        player()?.isSleepingIgnored = true

        player()?.setMetadata("vanished", FixedMetadataValue(plugin, true))

        hideUser()

        super.vanish(options)

        sendComponent(language.vanish.vanishStateUpdate, Placeholder.parsed("state", stateText()))
    }

    override fun unVanish(options: VanishOptions) {
        val unVanishEvent = BukkitUserUnVanishEvent(this, options)
        server.pluginManager.callEvent(unVanishEvent)
        if (unVanishEvent.isCancelled) return
        val options = unVanishEvent.options
        currentOptions = options

        if (ServerVersion.supports(9)) {
            player()?.isCollidable = true
        }
        player()?.isSleepingIgnored = false

        player()?.setMetadata("vanished", FixedMetadataValue(plugin, false))
        showUser()

        super.unVanish(options)

        sendComponent(language.vanish.vanishStateUpdate, Placeholder.parsed("state", stateText()))
    }

    override fun hasPermission(permission: String): Boolean {
        val luckPermsFeature = Features.getFeature<FeatureLuckPermsHook>()
        /*
        * I have to check if the player is op or not and luckperms feature is enabled so it doesn't disable all feature for op players
        * (bukkit permission check return true for all permissions if the player is op)
        * */
        if (permission.startsWith("sayanvanish.feature.disable.") && !luckPermsFeature.isActive() && player()?.isOp == true) {
            return false
        }
        // Can't use luckperms feature isActive per-player, because per-player features check for player permissions and it causes stackoverflow
        return if (luckPermsFeature.isActive() && luckPermsFeature.checkPermissionViaLuckPerms) {
            luckPermsFeature.hasPermission(uniqueId, permission)
        } else {
            player()?.hasPermission(permission) == true
        }
    }

    override fun sendComponent(content: String, vararg placeholder: TagResolver) {
        player()?.sendComponent(content, *placeholder)
    }

    override fun sendActionbar(content: String, vararg placeholder: TagResolver) {
        player()?.sendComponentActionbar(content, *placeholder)
    }

    fun hideUser() {
        for (onlinePlayer in onlinePlayers) {
            hideUser(onlinePlayer)
        }
        if (currentOptions.notifyStatusChangeToOthers) {
            for (otherUsers in SayanVanishBukkitAPI.getInstance().getOnlineUsers().filter { it.username != username && it.canSee(this) }) {
                otherUsers.sendComponent(language.vanish.vanishStateOther, Placeholder.parsed("player", username), Placeholder.parsed("state", stateText(true)))
            }
        }
    }

    fun hideUser(target: Player) {
        if (target.user() == null && (target.isOp || target.hasPermission(Permission.VANISH.permission()))) {
            target.getOrCreateUser()
        }
    }

    fun showUser() {
        for (onlinePlayer in onlinePlayers.filter { it.uniqueId != this.uniqueId }) {
            showUser(onlinePlayer)
        }
        if (currentOptions.notifyStatusChangeToOthers) {
            for (otherUsers in SayanVanishBukkitAPI.getInstance().getOnlineUsers().filter { it.username != this.username && it.canSee(this) }) {
                otherUsers.sendComponent(language.vanish.vanishStateOther, Placeholder.parsed("player", username), Placeholder.parsed("state", stateText(false)))
            }
        }
    }

    fun showUser(target: Player) {
        player()?.let { player ->
            showPlayer(target, player)
        }
    }

    private fun showPlayer(player: Player, target: Player) {
        if (ServerVersion.supports(9)) {
            player.showPlayer(plugin, target)
        } else {
            player.showPlayer(target)
        }
    }

    companion object {
        @JvmStatic
        fun fromUser(user: User): BukkitUser {
            return BukkitUser(user.uniqueId, user.username).apply {
                this.isOnline = user.isOnline
                this.isVanished = user.isVanished
                this.vanishLevel = user.vanishLevel
            }
        }
    }

}