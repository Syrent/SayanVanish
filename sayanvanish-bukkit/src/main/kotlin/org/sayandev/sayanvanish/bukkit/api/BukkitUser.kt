package org.sayandev.sayanvanish.bukkit.api

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
import org.sayandev.stickynote.bukkit.onlinePlayers
import org.sayandev.stickynote.bukkit.plugin
import org.sayandev.stickynote.bukkit.server
import org.sayandev.stickynote.bukkit.utils.AdventureUtils.component
import org.sayandev.stickynote.bukkit.utils.AdventureUtils.sendActionbar
import org.sayandev.stickynote.bukkit.utils.AdventureUtils.sendMessage
import org.sayandev.stickynote.bukkit.utils.ServerVersion
import org.sayandev.stickynote.lib.kyori.adventure.text.Component
import org.sayandev.stickynote.lib.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import java.util.*

open class BukkitUser(
    override val uniqueId: UUID,
    override var username: String
) : User {

    override var serverId = settings.general.serverId
    override var currentOptions = VanishOptions.defaultOptions()
    override var isVanished = false
    override var isOnline: Boolean = false
    override var vanishLevel: Int = 1
        get() = if (Features.getFeature<FeatureLevel>().levelMethod == FeatureLevel.LevelMethod.PERMISSION) {
            player()?.let { player ->
                player.effectivePermissions
                    .filter { it.permission.startsWith("sayanvanish.level.") }
                    .maxOfOrNull { it.permission.substringAfter("sayanvanish.level.").toIntOrNull() ?: 1 } ?: 1
            } ?: 1
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

        sendMessage(language.vanish.vanishStateUpdate.component(Placeholder.parsed("state", stateText())))
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

        sendMessage(language.vanish.vanishStateUpdate.component(Placeholder.parsed("state", stateText())))
    }

    override fun hasPermission(permission: String): Boolean {
        return player()?.hasPermission(permission) == true
    }

    override fun sendMessage(content: String) {
        player()?.sendMessage(content.component())
    }

    fun sendMessage(content: Component) {
        player()?.sendMessage(content)
    }

    override fun sendActionbar(content: String) {
        player()?.sendActionbar(content.component())
    }

    fun sendActionbar(content: Component) {
        player()?.sendActionbar(content)
    }

    fun hideUser() {
        for (onlinePlayer in onlinePlayers) {
            hideUser(onlinePlayer)
        }
        if (currentOptions.notifyStatusChangeToOthers) {
            for (otherUsers in SayanVanishBukkitAPI.getInstance().getOnlineUsers().filter { it.username != username && it.vanishLevel >= vanishLevel }) {
                otherUsers.sendMessage(language.vanish.vanishStateOther.component(Placeholder.parsed("player", username), Placeholder.parsed("state", stateText(true))))
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
            for (otherUsers in SayanVanishBukkitAPI.getInstance().getOnlineUsers().filter { it.username != this.username && it.vanishLevel >= this.vanishLevel }) {
                otherUsers.sendMessage(language.vanish.vanishStateOther.component(Placeholder.parsed("player", username), Placeholder.parsed("state", stateText(false))))
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