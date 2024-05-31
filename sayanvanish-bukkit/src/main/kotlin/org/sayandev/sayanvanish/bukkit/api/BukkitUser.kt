package org.sayandev.sayanvanish.bukkit.api

import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue
import org.sayandev.sayanvanish.api.Permission
import org.sayandev.sayanvanish.api.Platform
import org.sayandev.sayanvanish.api.User
import org.sayandev.sayanvanish.api.VanishOptions
import org.sayandev.sayanvanish.bukkit.VanishManager
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.getOrCreateUser
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.user
import org.sayandev.sayanvanish.bukkit.api.event.BukkitUserUnVanishEvent
import org.sayandev.sayanvanish.bukkit.api.event.BukkitUserVanishEvent
import org.sayandev.sayanvanish.bukkit.config.language
import org.sayandev.stickynote.bukkit.*
import org.sayandev.stickynote.bukkit.utils.AdventureUtils.component
import org.sayandev.stickynote.bukkit.utils.AdventureUtils.sendActionbar
import org.sayandev.stickynote.bukkit.utils.AdventureUtils.sendMessage
import org.sayandev.stickynote.lib.kyori.adventure.text.Component
import org.sayandev.stickynote.lib.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import java.util.*

open class BukkitUser(
    override val uniqueId: UUID,
    override var username: String
) : User {

    override var serverId = Platform.get().id
    override var currentOptions = VanishOptions.defaultOptions()
    override var isVanished = false
    override var isOnline: Boolean = false
    override var vanishLevel: Int = 1

    fun stateText(isVanished: Boolean = this.isVanished) = if (isVanished) "<green>ON</green>" else "<red>OFF</red>"

    fun player(): Player? = Bukkit.getPlayer(uniqueId)
    fun offlinePlayer(): OfflinePlayer = Bukkit.getOfflinePlayer(uniqueId)

    override fun vanish(options: VanishOptions) {
        val vanishEvent = BukkitUserVanishEvent(this, options)
        server.pluginManager.callEvent(vanishEvent)
        if (vanishEvent.isCancelled) return
        val options = vanishEvent.options
        currentOptions = options

        if (options.sendMessage) {
            val quitMessage = VanishManager.generalQuitMessage
            if (quitMessage != null) {
                for (onlinePlayer in onlinePlayers) {
                    onlinePlayer.sendMessage(quitMessage)
                }
            }
        }

        player()?.isCollidable = false
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

        if (options.sendMessage) {
            val joinMessage = VanishManager.generalJoinMessage
            if (joinMessage != null) {
                for (onlinePlayer in onlinePlayers) {
                    onlinePlayer.sendMessage(joinMessage)
                }
            }
        }

        player()?.isCollidable = true
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
        if (currentOptions.notifyOthers) {
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
        if (currentOptions.notifyOthers) {
            for (otherUsers in SayanVanishBukkitAPI.getInstance().getOnlineUsers().filter { it.username != username && it.vanishLevel >= vanishLevel }) {
                otherUsers.sendMessage(language.vanish.vanishStateOther.component(Placeholder.parsed("player", username), Placeholder.parsed("state", stateText(false))))
            }
        }
    }

    fun showUser(target: Player) {
        player()?.let { player ->
            target.showPlayer(plugin, player)
            NMSUtils.sendPacket(target, PacketUtils.getAddEntityPacket(NMSUtils.getServerPlayer(player)))
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