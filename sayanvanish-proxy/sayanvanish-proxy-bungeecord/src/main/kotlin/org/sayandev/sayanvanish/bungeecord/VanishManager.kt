package org.sayandev.sayanvanish.bungeecord

import net.md_5.bungee.api.event.PlayerDisconnectEvent
import net.md_5.bungee.api.event.ServerConnectedEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import org.sayandev.sayanvanish.api.BasicUser
import org.sayandev.sayanvanish.api.Platform
import org.sayandev.sayanvanish.bungeecord.api.SayanVanishBungeeAPI
import org.sayandev.sayanvanish.bungeecord.api.SayanVanishBungeeAPI.Companion.getOrCreateUser

object VanishManager : Listener {

    @EventHandler
    fun onPostLogin(event: ServerConnectedEvent) {
        val player = event.player ?: return
        SayanVanishBungeeAPI.getInstance().database.addBasicUser(BasicUser.create(player.uniqueId, player.name, event.server?.info?.name ?: Platform.get().id))
        val user = player.getOrCreateUser()
    }

    fun onDisconnect(event: PlayerDisconnectEvent) {
        val player = event.player ?: return
        SayanVanishBungeeAPI.getInstance().database.removeBasicUser(player.uniqueId)
    }

}