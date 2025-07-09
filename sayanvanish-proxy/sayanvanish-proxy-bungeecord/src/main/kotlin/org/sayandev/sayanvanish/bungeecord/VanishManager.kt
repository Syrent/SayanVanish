package org.sayandev.sayanvanish.bungeecord

import net.md_5.bungee.api.event.PlayerDisconnectEvent
import net.md_5.bungee.api.event.ServerConnectedEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import org.sayandev.sayanvanish.api.User
import org.sayandev.sayanvanish.api.Platform
import org.sayandev.sayanvanish.bungeecord.api.SayanVanishBungeeAPI
import org.sayandev.sayanvanish.bungeecord.api.SayanVanishBungeeAPI.getOrCreateUser
import org.sayandev.stickynote.bungeecord.launch

object VanishManager : Listener {

    @EventHandler
    fun onPostLogin(event: ServerConnectedEvent) {
        val player = event.player ?: return
        launch {
            SayanVanishBungeeAPI.getDatabase().saveUser(User.of(player.uniqueId, player.name, true, player.server.info.name ?: Platform.get().id))
            player.getOrCreateUser()
        }
    }

    fun onDisconnect(event: PlayerDisconnectEvent) {
        val player = event.player ?: return
        launch {
            SayanVanishBungeeAPI.getDatabase().removeUser(player.uniqueId)
        }
    }

}