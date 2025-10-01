package org.sayandev.sayanvanish.velocity

import com.github.shynixn.mccoroutine.velocity.SuspendingPluginContainer
import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import org.sayandev.sayanvanish.api.Platform
import org.sayandev.sayanvanish.api.SayanVanishAPI
import org.sayandev.sayanvanish.api.VanishAPI
import org.sayandev.sayanvanish.proxy.config.Settings
import org.sayandev.sayanvanish.proxy.config.language
import org.sayandev.sayanvanish.velocity.api.SayanVanishVelocityAPI
import org.sayandev.sayanvanish.velocity.command.SayanVanishProxyCommandVelocity
import org.sayandev.stickynote.loader.velocity.StickyNoteVelocityLoader
import org.sayandev.stickynote.velocity.launch
import org.sayandev.stickynote.velocity.registerListener
import org.slf4j.Logger
import java.io.File
import java.nio.file.Path

class SayanVanishPlugin @Inject constructor(
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
        setInstance(this)
        Platform.get().rootDirectory = dataDirectory.toFile()

        if (!Platform.setAndRegister(VelocityPlatform())) return

        SayanVanishAPI.initialize()

        Settings.reload()
        language

        Platform.get().serverId = Settings.get().general.serverId

        SayanVanishProxyCommandVelocity()

        registerListener(VanishManager)

        // TODO: move this somewhere else. it's not something to be on main class and the main initialization method
        launch {
            if (Settings.get().general.purgeOnlineHistoryOnStartup) {
                for (onlineServer in server.allServers) {
                    VanishAPI.get().getDatabase().purgeUsers(onlineServer.serverInfo.name)
                }
                VanishAPI.get().getDatabase().purgeUsers(Settings.get().general.serverId)
            }

            if (Settings.get().general.purgeUsersOnStartup) {
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
        
        private lateinit var instance: SayanVanishPlugin

        fun getInstance(): SayanVanishPlugin {
            return instance
        }

        private fun setInstance(plugin: SayanVanishPlugin) {
            instance = plugin
        }
    }

}