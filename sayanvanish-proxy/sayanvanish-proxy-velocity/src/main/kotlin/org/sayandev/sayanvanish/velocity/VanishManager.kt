package org.sayandev.sayanvanish.velocity

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.player.ServerPostConnectEvent
import org.sayandev.sayanvanish.api.BasicUser
import org.sayandev.sayanvanish.api.Platform
import org.sayandev.sayanvanish.velocity.api.SayanVanishVelocityAPI
import org.sayandev.sayanvanish.velocity.api.SayanVanishVelocityAPI.Companion.getOrCreateUser
import kotlin.jvm.optionals.getOrNull

object VanishManager {

    @Subscribe
    private fun onPostLogin(event: ServerPostConnectEvent) {
        val player = event.player ?: return
        SayanVanishVelocityAPI.getInstance().addBasicUser(BasicUser.create(player.uniqueId, player.username, player.currentServer.getOrNull()?.serverInfo?.name ?: Platform.get().id))
        val user = player.getOrCreateUser()
    }

    @Subscribe
    private fun onDisconnect(event: DisconnectEvent) {
        val player = event.player ?: return
        SayanVanishVelocityAPI.getInstance().removeBasicUser(player.uniqueId)
    }

}