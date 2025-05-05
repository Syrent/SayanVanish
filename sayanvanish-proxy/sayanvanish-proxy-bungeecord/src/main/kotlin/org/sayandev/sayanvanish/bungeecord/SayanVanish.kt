package org.sayandev.sayanvanish.bungeecord

import BungeePlatformAdapter
import kotlinx.coroutines.runBlocking
import net.md_5.bungee.api.plugin.Plugin
import org.sayandev.sayanvanish.api.Platform
import org.sayandev.sayanvanish.bungeecord.api.SayanVanishBungeeAPI
import org.sayandev.sayanvanish.proxy.config.settings
import org.sayandev.stickynote.bungeecord.dataDirectory
import org.sayandev.stickynote.bungeecord.registerListener
import org.sayandev.stickynote.bungeecord.server
import org.sayandev.stickynote.loader.bungee.StickyNoteBungeeLoader

class SayanVanish : Plugin() {

    override fun onEnable() {
        StickyNoteBungeeLoader(this)
        // Do NOT set server id here, SettingsConfig can't be used because it depends on Platform rootDirectory
        Platform.get().rootDirectory = dataDirectory
        if (!Platform.setAndRegister(Platform("bungeecord", this.description.name, logger, dataDirectory, "", BungeePlatformAdapter))) return
        Platform.get().serverId = settings.general.serverId

        SayanVanishBungeeAPI

        registerListener(VanishManager)

        runBlocking {
            if (settings.general.purgeOnlineHistoryOnStartup) {
                for (onlineServer in server.servers) {
                    SayanVanishBungeeAPI.getDatabase().purgeUsers(onlineServer.value.name)
                }
                SayanVanishBungeeAPI.getDatabase().purgeUsers(settings.general.serverId)
            }
        }
    }
}