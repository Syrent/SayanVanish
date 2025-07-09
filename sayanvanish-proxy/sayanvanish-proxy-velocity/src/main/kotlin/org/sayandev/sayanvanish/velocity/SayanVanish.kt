package org.sayandev.sayanvanish.velocity

import com.github.shynixn.mccoroutine.velocity.SuspendingPluginContainer
import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import org.sayandev.sayanvanish.api.Platform
import org.sayandev.sayanvanish.api.VanishAPI
import org.sayandev.sayanvanish.proxy.config.language
import org.sayandev.sayanvanish.proxy.config.settings
import org.sayandev.sayanvanish.velocity.api.SayanVanishVelocityAPI
import org.sayandev.sayanvanish.velocity.command.SayanVanishProxyCommandVelocity
import org.sayandev.sayanvanish.velocity.health.HealthCheckMessageSubscriber
import org.sayandev.sayanvanish.velocity.health.ServerInfoPublisher
import org.sayandev.stickynote.loader.velocity.StickyNoteVelocityLoader
import org.sayandev.stickynote.velocity.launch
import org.sayandev.stickynote.velocity.registerListener
import org.slf4j.Logger
import java.io.File
import java.nio.file.Path

lateinit var sayanvanish: SayanVanish

class SayanVanish @Inject constructor(
    val suspendingPluginContainer: SuspendingPluginContainer
) {

    @Inject
    lateinit var server: ProxyServer

    @Inject
    lateinit var logger: Logger

    @Inject
    @DataDirectory lateinit var dataDirectory: Path

    @Subscribe
    fun onProxyInitialize(event: ProxyInitializeEvent) {
        StickyNoteVelocityLoader(this, PLUGIN_ID, server, logger, dataDirectory)
        suspendingPluginContainer.initialize(this)
        sayanvanish = this
        Platform.get().rootDirectory = dataDirectory.toFile()

        if (!Platform.setAndRegister(Platform("velocity", PLUGIN_ID, java.util.logging.Logger.getLogger("sayanvanish"), dataDirectory.toFile(), "", VelocityPlatformAdapter))) return

        settings
        language

        Platform.get().serverId = settings.general.serverId

        SayanVanishVelocityAPI

        HealthCheckMessageSubscriber().register()
        ServerInfoPublisher

        SayanVanishProxyCommandVelocity()


        registerListener(VanishManager)

        // TODO: move this somewhere else. it's not something to be on main class and the main initialization method
        launch {
            if (settings.general.purgeOnlineHistoryOnStartup) {
                for (onlineServer in server.allServers) {
                    VanishAPI.get().getDatabase().purgeUsers(onlineServer.serverInfo.name)
                }
                VanishAPI.get().getDatabase().purgeUsers(settings.general.serverId)
            }

            if (settings.general.purgeUsersOnStartup) {
                for (user in VanishAPI.get().getDatabase().getUsers().await()) {
                    user.isOnline = false
                    user.save()
                }
            }
        }
    }

    fun pluginFile(): File? {
        return dataDirectory.parent.toFile().listFiles().filter { it.isFile }.find { it.name.lowercase().contains("sayanvanish") && it.extension == "jar" }
    }

    companion object {
        const val PLUGIN_ID = "sayanvanish"
    }

}