package org.sayandev.sayanvanish.bukkit.health

import org.sayandev.sayanvanish.api.`database-old`.databaseConfig
import org.sayandev.sayanvanish.api.health.HealthCheckData
import org.sayandev.sayanvanish.bukkit.config.settings
import org.sayandev.stickynote.bukkit.messaging.publisher.PluginMessagePublisher
import org.sayandev.stickynote.bukkit.plugin

object ServerInfoPublisher : PluginMessagePublisher<Unit, HealthCheckData.ServerInfo>(
    plugin.name.lowercase(),
    "info",
    Unit::class.java,
    HealthCheckData.ServerInfo::class.java,
    true
) {
    override fun handle(payload: Unit): HealthCheckData.ServerInfo {
        return HealthCheckData.ServerInfo(
            settings.general.serverId,
            null,
            settings.general.proxyMode,
            databaseConfig.method,
            databaseConfig.sql.method,
            System.currentTimeMillis()
        )
    }
}