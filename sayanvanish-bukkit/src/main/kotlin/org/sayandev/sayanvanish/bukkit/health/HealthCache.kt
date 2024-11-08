package org.sayandev.sayanvanish.bukkit.health

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.sayandev.sayanvanish.api.database.DatabaseMethod
import org.sayandev.sayanvanish.api.database.sql.SQLConfig
import org.sayandev.sayanvanish.api.health.HealthCheckData
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.user
import org.sayandev.sayanvanish.bukkit.config.settings
import org.sayandev.stickynote.bukkit.extension.sendComponent
import org.sayandev.stickynote.bukkit.log
import org.sayandev.stickynote.bukkit.onlinePlayers
import org.sayandev.stickynote.bukkit.registerSuspendingListener
import org.sayandev.stickynote.bukkit.warn
import org.sayandev.stickynote.core.messaging.publisher.PayloadWrapper
import org.sayandev.stickynote.core.utils.CoroutineUtils.awaitWithTimeout

object HealthCache: Listener {
    var cache: HealthCheckData? = null

    init {
        registerSuspendingListener(this)
    }

    suspend fun updateCache(player: Player?): HealthCheckData? {
        val player = player ?: onlinePlayers.firstOrNull()
        if (player == null) {
            if (cache != null) {
                log("Couldn't find any online player to update the cache. Using the previous cache.")
                return cache
            } else {
                warn("Couldn't find any online player to update the cache.")
                return null
            }
        }
        val healthData = HealthCheckRequestPublisher.publish(player, PayloadWrapper(Unit, PayloadWrapper.State.PROXY)).awaitWithTimeout(5000) ?: let {
            return null
        }
        cache?.let { validCache ->
            for (server in healthData.servers.servers.toList()) {
                if (server.id == null) {
                    val cacheServer = validCache.servers.servers.find { it.name == server.name } ?: continue
                    healthData.servers.servers.remove(server)
                    healthData.servers.servers.add(cacheServer)
                }
            }
        }
        cache = healthData
        return healthData
    }

    @EventHandler
    private suspend fun onPlayerJoin(event: PlayerJoinEvent) {
        event.player.user() ?: return
        updateCache(event.player)
    }

    suspend fun sendHealthReport(sender: CommandSender) {
        if (!settings.general.proxyMode) {
            sender.sendComponent("<red>You need to enable `proxy-mode` to use this feature. Make sure you've checked the installation guide at <gold><click:open_url:'https://docs.sayandev.org/sayanvanish/installation'>SayanVanish docs</click></gold>")
            return
        }

        val healthData = updateCache(sender as? Player)
        if (healthData == null) {
            sender.sendComponent("<red>Couldn't get any response from your proxy. This feature only works with Velocity proxies. If you're using Velocity as your proxy please report this issue on SayanVanish github page.")
            return
        }

        val proxy = healthData.proxy
        sender.sendComponent("<green>Proxy:")
        sender.sendComponent("<gray> - <yellow>Database Method: ${proxy.databaseMethod}")
        if (proxy.databaseMethod == DatabaseMethod.SQL) {
            sender.sendComponent("<gray> - <yellow>SQL Method: ${proxy.sqlMethod}")
        }

        val servers = healthData.servers.servers
        if (servers.isNotEmpty()) {
            sender.sendComponent("<green>Servers:")
            for (server in servers) {
                if (server.id == null) {
                    sender.sendComponent("<gray> - <yellow>${server.name} <red>(UNRECHABLE)")
                    continue
                }
                sender.sendComponent("<gray> - <yellow>${server.name} <gray>(${timeAgo(server.timestamp)} since last update)")
                sender.sendComponent("<gray>   - <yellow>Proxy Mode: ${server.enabledProxyMode}")
                sender.sendComponent("<gray>   - <yellow>Database Method: ${server.databaseMethod}")
                if (server.databaseMethod == DatabaseMethod.SQL) {
                    sender.sendComponent("<gray>   - <yellow>SQL Method: ${server.sqlMethod}")
                }
            }

            val duplicateServers = servers.groupBy { it.id }.filter { it.value.size > 1 }
            for (duplicateServer in duplicateServers) {
                sender.sendComponent("<red>Found duplicate servers with id <yellow>${duplicateServer.key}")
                for (server in duplicateServer.value) {
                    sender.sendComponent("<gray> - <yellow>${server.name}")
                }
                sender.sendComponent("<red>Make sure to change the server id in your `settings.yml` file.")
            }

            val sqliteServers = servers.filter { it.databaseMethod == DatabaseMethod.SQL && it.sqlMethod == SQLConfig.SQLMethod.SQLITE }
            if (sqliteServers.isNotEmpty()) {
                sender.sendComponent("<red>Found servers using SQLite as their database method.")
                for (server in sqliteServers) {
                    sender.sendComponent("<gray> - <yellow>${server.name}")
                }
                sender.sendComponent("<red>SQLite will not sync data between servers. Make sure to change the database method to MySQL or Redis.")
            }

            val unrechableServers = servers.filter { it.id == null }
            if (unrechableServers.isNotEmpty()) {
                sender.sendComponent("<red>Found servers that are unreachable.")
                for (server in unrechableServers) {
                    sender.sendComponent("<gray> - <yellow>${server.name}")
                }
                sender.sendComponent("<red>These server either don't have SayanVanish installed or is offline.")
            }
        } else {
            sender.sendComponent("<red>No servers found.")
        }
    }

    private fun timeAgo(time: Long): String {
        val diff = System.currentTimeMillis() - time
        val hours = diff / 3600000
        val minutes = (diff % 3600000) / 60000
        val seconds = (diff % 60000) / 1000
        val builder = StringBuilder()
        if (hours > 0) {
            builder.append("${hours}h ")
        }
        if (minutes > 0) {
            builder.append("${minutes}m ")
        }
        if (seconds > 0) {
            builder.append("${seconds}s")
        }
        return builder.toString()
    }
}