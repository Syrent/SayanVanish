package org.sayandev.sayanvanish.bukkit

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.sayandev.sayanvanish.api.VanishAPI
import org.sayandev.sayanvanish.bukkit.api.BukkitPlatformAdapter
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.getCachedOrCreateUser
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.getOrAddUser
import org.sayandev.sayanvanish.bukkit.config.Settings
import org.sayandev.stickynote.bukkit.launch
import org.sayandev.stickynote.bukkit.registerListener

object VanishManager : Listener {

    init {
        registerListener(this)
    }

    @EventHandler
    private fun addUserOnJoin(event: PlayerJoinEvent) {
        if (Settings.get().general.proxyMode) return

        val player = event.player
        launch {
            player.getOrAddUser()
        }
    }

    @EventHandler
    private fun makeUserOfflineOnQuit(event: PlayerQuitEvent) {
        if (Settings.get().general.proxyMode) return

        val player = event.player

        launch {
            val user = player.getCachedOrCreateUser()
            user.isOnline = false
            user.save()
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun hideVanishedPlayersOnJoin(event: PlayerJoinEvent) {
        for (user in VanishAPI.get().getCacheService().getVanishUsers().getVanished().map { BukkitPlatformAdapter.adapt(it) }.filter { it.player() != null }) {
            user.hideUser(event.player)
        }
    }

}