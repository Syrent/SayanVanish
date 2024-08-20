package org.sayandev.sayanvanish.velocity

import com.alessiodp.libby.VelocityLibraryManager
import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import org.sayandev.sayanvanish.api.Platform
import org.sayandev.sayanvanish.proxy.config.settings
import org.sayandev.sayanvanish.velocity.api.SayanVanishVelocityAPI
import org.sayandev.stickynote.loader.bungee.StickyNoteVelocityLoader
import org.sayandev.stickynote.velocity.StickyNote
import org.sayandev.stickynote.velocity.registerListener
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Path
import java.util.concurrent.TimeUnit

class SayanVanish @Inject constructor(
    val server: ProxyServer,
    val logger: Logger,
    @DataDirectory val dataDirectory: Path
) {

    @Subscribe
    fun onProxyInitialize(event: ProxyInitializeEvent) {
        StickyNoteVelocityLoader(this, PLUGIN_ID, server, logger, dataDirectory)

        Platform.setAndRegister(Platform("velocity", java.util.logging.Logger.getLogger("sayanvanish"), dataDirectory.toFile(), settings.general.serverId))
        SayanVanishVelocityAPI


        registerListener(VanishManager)

        if (settings.general.purgeOnlineHistoryOnStartup) {
            for (onlineServer in server.allServers) {
                SayanVanishVelocityAPI.getInstance().database.purgeBasic(onlineServer.serverInfo.name)
            }
            SayanVanishVelocityAPI.getInstance().database.purgeBasic(settings.general.serverId)
        }

        StickyNote.run({
            SayanVanishVelocityAPI.getInstance().database.updateBasicCacheAsync()
        }, settings.general.basicCacheUpdatePeriodMillis, TimeUnit.MILLISECONDS, settings.general.basicCacheUpdatePeriodMillis, TimeUnit.MILLISECONDS)

        StickyNote.run({
            SayanVanishVelocityAPI.getInstance().database.updateCacheAsync()
        }, settings.general.cacheUpdatePeriodMillis, TimeUnit.MILLISECONDS, settings.general.cacheUpdatePeriodMillis, TimeUnit.MILLISECONDS)
    }

    companion object {
        const val PLUGIN_ID = "sayanvanish"
    }

}