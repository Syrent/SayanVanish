package org.sayandev.sayanvanish.bungeecord

import net.md_5.bungee.api.plugin.Plugin
import org.sayandev.sayanvanish.api.Platform
import org.sayandev.sayanvanish.bungeecord.api.SayanVanishBungeeAPI
import org.sayandev.sayanvanish.proxy.config.settings
import org.sayandev.sayanvanish.velocity.VanishManager
import org.sayandev.stickynote.bungeecord.StickyNote
import org.sayandev.stickynote.bungeecord.dataDirectory
import org.sayandev.stickynote.bungeecord.registerListener
import org.sayandev.stickynote.bungeecord.server
import org.sayandev.stickynote.loader.bungee.StickyNoteBungeeLoader
import java.util.concurrent.TimeUnit

class SayanVanish : Plugin() {

    override fun onEnable() {
        StickyNoteBungeeLoader(this)
        Platform.setAndRegister(Platform("bungeecord", logger, dataDirectory, settings.general.serverId))

        SayanVanishBungeeAPI()

        registerListener(VanishManager)

        if (settings.general.purgeOnlineHistoryOnStartup) {
            for (onlineServer in server.servers) {
                SayanVanishBungeeAPI.getInstance().database.purgeBasic(onlineServer.value.name)
            }
            SayanVanishBungeeAPI.getInstance().database.purgeBasic(settings.general.serverId)
        }

        StickyNote.run({
            SayanVanishBungeeAPI.getInstance().database.updateBasicCacheAsync()
        }, settings.general.basicCacheUpdatePeriodMillis, settings.general.basicCacheUpdatePeriodMillis, TimeUnit.MILLISECONDS)

        StickyNote.run({
            SayanVanishBungeeAPI.getInstance().database.updateCacheAsync()
        }, settings.general.cacheUpdatePeriodMillis, settings.general.cacheUpdatePeriodMillis, TimeUnit.MILLISECONDS)
    }
}