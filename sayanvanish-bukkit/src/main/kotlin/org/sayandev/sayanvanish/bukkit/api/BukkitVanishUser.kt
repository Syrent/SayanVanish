package org.sayandev.sayanvanish.bukkit.api

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.permissions.PermissionDefault
import org.sayandev.sayanvanish.api.Permission
import org.sayandev.sayanvanish.api.VanishUser
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
import org.sayandev.sayanvanish.bukkit.utils.PlayerUtils.sendComponent
import org.sayandev.stickynote.bukkit.extension.sendComponentActionbar
import org.sayandev.stickynote.bukkit.hasPlugin
import org.sayandev.stickynote.bukkit.onlinePlayers
import org.sayandev.stickynote.bukkit.plugin
import org.sayandev.stickynote.bukkit.server
import org.sayandev.stickynote.bukkit.utils.ServerVersion
import java.util.*

open class BukkitVanishUser(
    override val uniqueId: UUID,
    override var username: String
) : VanishUser {

    override var serverId = settings.general.serverId
    override var currentOptions = VanishOptions.defaultOptions()
    override var isVanished = false
    override var isOnline: Boolean = if (!settings.general.proxyMode) {
        Bukkit.getPlayer(uniqueId) != null
    } else {
        SayanVanishAPI.getDatabase().hasUser(uniqueId, true)
    }
    override var vanishLevel: Int = 0
        get() = if (Features.getFeature<FeatureLevel>().levelMethod == FeatureLevel.LevelMethod.PERMISSION) {
            player()?.let { player ->
                player.effectivePermissions
                    .filter { it.permission.startsWith("sayanvanish.level.") }
                    .maxOfOrNull { it.permission.substringAfter("sayanvanish.level.").toIntOrNull() ?: field }
                    ?: if (hasPermission(Permission.VANISH)) 1 else {
                        if (isVanished) 1 else field
                    }
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

        super.disappear(options)

        // order matters - don't move hideUser before vanish (hideUser have a canSee check for vanish state notify)
        hideUser()

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

        player()?.removeMetadata("vanished", plugin)
        showUser()

        super.appear(options)

        sendComponent(language.vanish.vanishStateUpdate, Placeholder.parsed("state", stateText()))
    }

    override fun hasPermission(permission: String): Boolean {
        return if (hasPlugin("LuckPerms")) {
            val luckPermsFeature = Features.getFeature<FeatureLuckPermsHook>()
            /*
            * I have to check if the player is op or not and luckperms feature is enabled so it doesn't disable all feature for op players
            * (bukkit permission check return true for all permissions if the player is op)
            * */
            // Can't use luckperms feature isActive per-player, because per-player features check for player permissions and it causes stackoverflow
            if (luckPermsFeature.isActive() && luckPermsFeature.checkPermissionViaLuckPerms) {
                luckPermsFeature.hasPermission(uniqueId, permission)
            } else {
                if (permission.startsWith("sayanvanish.feature.disable.")) {
                    return if (luckPermsFeature.isActive() && luckPermsFeature.checkPermissionViaLuckPermsFeatures) {
                        luckPermsFeature.hasPermission(uniqueId, permission)
                    } else {
                        false
                    }
                }
                player()?.hasPermission(org.bukkit.permissions.Permission(permission, PermissionDefault.FALSE)) == true
            }
        } else {
            if (permission.startsWith("sayanvanish.feature.disable.")) {
                return false
            }
            player()?.hasPermission(org.bukkit.permissions.Permission(permission, PermissionDefault.FALSE)) == true
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
        fun fromUser(vanishUser: VanishUser): BukkitVanishUser {
            return BukkitVanishUser(vanishUser.uniqueId, vanishUser.username).apply {
                this.isOnline = vanishUser.isOnline
                this.isVanished = vanishUser.isVanished
                this.vanishLevel = vanishUser.vanishLevel
            }
        }
    }

}