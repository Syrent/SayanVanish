package org.sayandev.sayanvanish.velocity

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.player.ServerPostConnectEvent
import org.sayandev.sayanvanish.api.BasicUser
import org.sayandev.sayanvanish.api.Platform
import org.sayandev.sayanvanish.velocity.api.SayanVanishVelocityAPI
import org.sayandev.sayanvanish.velocity.api.SayanVanishVelocityAPI.Companion.getOrCreateUser
import org.sayandev.sayanvanish.velocity.event.VelocityUserUnVanishEvent
import org.sayandev.sayanvanish.velocity.event.VelocityUserVanishEvent
import org.sayandev.stickynote.velocity.server
import kotlin.jvm.optionals.getOrNull

object VanishManager {

    @Subscribe
    private fun onPostLogin(event: ServerPostConnectEvent) {
        val player = event.player ?: return
        SayanVanishVelocityAPI.getInstance().database.addBasicUser(BasicUser.create(player.uniqueId, player.username, player.currentServer.getOrNull()?.serverInfo?.name ?: Platform.get().id))
        val user = player.getOrCreateUser()
        if (user.isVanished) {
            server.eventManager.fireAndForget(VelocityUserVanishEvent(user, user.currentOptions))
        } else {
            server.eventManager.fireAndForget(VelocityUserUnVanishEvent(user, user.currentOptions))
        }
    }

    @Subscribe
    private fun onDisconnect(event: DisconnectEvent) {
        val player = event.player ?: return
        SayanVanishVelocityAPI.getInstance().database.removeBasicUser(player.uniqueId)
    }

}