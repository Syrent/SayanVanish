package org.sayandev.sayanvanish.velocity

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.player.ServerPostConnectEvent
import kotlinx.coroutines.awaitAll
import org.sayandev.sayanvanish.api.VanishAPI
import org.sayandev.sayanvanish.velocity.api.SayanVanishVelocityAPI.Companion.getOrCreateVanishUser
import org.sayandev.sayanvanish.velocity.api.VelocityVanishUser.Companion.getVanishUser
import org.sayandev.sayanvanish.velocity.api.VelocityVanishUser.Companion.velocityAdapt
import org.sayandev.sayanvanish.velocity.event.VelocityUserUnVanishEvent
import org.sayandev.sayanvanish.velocity.event.VelocityUserVanishEvent
import org.sayandev.stickynote.velocity.launch
import org.sayandev.stickynote.velocity.server
import kotlin.jvm.optionals.getOrNull

object VanishManager {

    @Subscribe
    private fun onPostLogin(event: ServerPostConnectEvent) {
        val player = event.player ?: return
        launch {
            val user = player.getOrCreateVanishUser().velocityAdapt()
            player.currentServer.getOrNull()?.serverInfo?.name?.takeUnless { it.isEmpty() }?.let { serverName ->
                user.serverId = serverName
            }
            user.isOnline = true
            user.saveAndSync().awaitAll()

            if (user.isVanished) {
                server.eventManager.fireAndForget(VelocityUserVanishEvent(user, user.currentOptions))
            } else {
                server.eventManager.fireAndForget(VelocityUserUnVanishEvent(user, user.currentOptions))
            }
        }
    }

    @Subscribe
    private fun onDisconnect(event: DisconnectEvent) {
        val player = event.player ?: return
        launch {
            player.getVanishUser()?.velocityAdapt()?.let { user ->
                user.isOnline = false
                user.saveAndSync().awaitAll()
            }
            VanishAPI.get().getDatabase().removeUser(player.uniqueId).await()
        }
    }

}
