package org.sayandev.sayanvanish.bukkit

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.sayandev.sayanvanish.api.BasicUser
import org.sayandev.sayanvanish.api.SayanVanishAPI
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI
import org.sayandev.sayanvanish.bukkit.config.settings
import org.sayandev.stickynote.bukkit.registerListener

object VanishManager : Listener {

    init {
        registerListener(this)
    }

    @EventHandler
    private fun addBasicUserOnJoin(event: PlayerJoinEvent) {
        if (settings.general.proxyMode) return

        val player = event.player
        SayanVanishAPI.getInstance().database.addBasicUser(BasicUser.create(player.uniqueId, player.name, null))
    }

    @EventHandler
    private fun removeBasicUserOnQuit(event: PlayerQuitEvent) {
        if (settings.general.proxyMode) return

        val player = event.player
        SayanVanishAPI.getInstance().database.cache.remove(player.uniqueId)
        SayanVanishAPI.getInstance().database.removeBasicUser(player.uniqueId)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun hideVanishedPlayersOnJoin(event: PlayerJoinEvent) {
        for (user in SayanVanishBukkitAPI.getInstance().database.getUsers().filter { it.isVanished && it.player() != null }) {
            user.hideUser(event.player)
        }
    }

}