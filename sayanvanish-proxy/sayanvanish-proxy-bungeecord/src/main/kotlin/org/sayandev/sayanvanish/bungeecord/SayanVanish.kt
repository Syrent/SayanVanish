package org.sayandev.sayanvanish.bungeecord

import net.md_5.bungee.api.plugin.Plugin
import org.sayandev.sayanvanish.api.Platform
import org.sayandev.sayanvanish.api.SayanVanishAPI
import org.sayandev.sayanvanish.api.database.DatabaseMethod
import org.sayandev.sayanvanish.api.database.databaseConfig
import org.sayandev.sayanvanish.api.database.sql.SQLDatabase
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
        // Do NOT set server id here, SettingsConfig can't be used because it depends on Platform rootDirectory
        Platform.setAndRegister(Platform("bungeecord", logger, dataDirectory, ""))
        Platform.get().serverId = settings.general.serverId

        SayanVanishBungeeAPI()

        registerListener(VanishManager)

        if (settings.general.purgeOnlineHistoryOnStartup) {
            for (onlineServer in server.servers) {
                SayanVanishBungeeAPI.getInstance().database.purgeBasic(onlineServer.value.name)
            }
            SayanVanishBungeeAPI.getInstance().database.purgeBasic(settings.general.serverId)
        }

        StickyNote.run({
            SayanVanishBungeeAPI.getInstance().database.getUsersAsync { users ->
                SayanVanishBungeeAPI.getInstance().database.cache = users.associateBy { it.uniqueId }.toMutableMap()
                SayanVanishAPI.getInstance().database.cache = users.associateBy { it.uniqueId }.toMutableMap()
            }
        }, settings.general.basicCacheUpdatePeriodMillis, settings.general.basicCacheUpdatePeriodMillis, TimeUnit.MILLISECONDS)

        StickyNote.run({
            if (databaseConfig.method == DatabaseMethod.SQL) {
                SayanVanishBungeeAPI.getInstance().database.getBasicUsersAsync { users ->
                    (SayanVanishBungeeAPI.getInstance().database as SQLDatabase).basicCache = users.associateBy { it.uniqueId }.toMutableMap()
                    (SayanVanishAPI.getInstance().database as SQLDatabase).basicCache = users.associateBy { it.uniqueId }.toMutableMap()
                }
            }
        }, settings.general.cacheUpdatePeriodMillis, settings.general.cacheUpdatePeriodMillis, TimeUnit.MILLISECONDS)
    }
}