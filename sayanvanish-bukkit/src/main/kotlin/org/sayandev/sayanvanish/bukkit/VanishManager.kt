package org.sayandev.sayanvanish.bukkit

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.*
import org.sayandev.sayanvanish.api.BasicUser
import org.sayandev.sayanvanish.api.SayanVanishAPI
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI
import org.sayandev.sayanvanish.bukkit.config.settings
import org.sayandev.stickynote.bukkit.*

object VanishManager : Listener {

    // TODO: Remove this
    private const val REMOVAL_MESSAGE_ID = "SAYANVANISH_DISABLE_MESSAGE"
    var generalJoinMessage: String? = null
    var generalQuitMessage: String? = null

    init {
        registerListener(this)
    }

    @EventHandler
    private fun addBasicUserOnJoin(event: PlayerJoinEvent) {
        if (settings.general.proxyMode) return

        val player = event.player
        SayanVanishAPI.getInstance().addBasicUser(BasicUser.create(player.uniqueId, player.name, null))
    }

    @EventHandler
    private fun removeBasicUserOnQuit(event: PlayerJoinEvent) {
        if (settings.general.proxyMode) return

        val player = event.player
        SayanVanishAPI.getInstance().removeBasicUser(player.uniqueId)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun hideVanishedPlayersOnJoin(event: PlayerJoinEvent) {
        for (user in SayanVanishBukkitAPI.getInstance().getUsers { it.isVanished && it.player() != null }) {
            user.hideUser(event.player)
        }
    }

}