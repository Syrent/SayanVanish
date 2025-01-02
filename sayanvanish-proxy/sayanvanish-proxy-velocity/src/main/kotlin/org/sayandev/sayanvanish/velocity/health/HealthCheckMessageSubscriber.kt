package org.sayandev.sayanvanish.velocity.health

import kotlinx.coroutines.CompletableDeferred
import org.sayandev.sayanvanish.api.database.DatabaseMethod
import org.sayandev.sayanvanish.api.database.databaseConfig
import org.sayandev.sayanvanish.api.database.sql.SQLConfig
import org.sayandev.sayanvanish.api.health.HealthCheckData
import org.sayandev.stickynote.core.messaging.publisher.PayloadWrapper
import org.sayandev.stickynote.core.utils.CoroutineUtils.awaitWithTimeout
import org.sayandev.stickynote.velocity.messaging.ProxySubscriber
import org.sayandev.stickynote.velocity.plugin

class HealthCheckMessageSubscriber: ProxySubscriber<Unit, HealthCheckData>(
    plugin.container.description.name.get().lowercase(),
    "hc",
    Unit::class.java
) {

    override suspend fun onSubscribe(payload: Unit): CompletableDeferred<HealthCheckData> {
        val proxyInfo = HealthCheckData.ProxyInfo(
            databaseConfig.method,
            databaseConfig.sql.method
        )

        val servers = HealthCheckData.Servers(mutableListOf())

        for (server in plugin.server.allServers) {
            servers.servers.add((ServerInfoPublisher.publish(server, PayloadWrapper(Unit, PayloadWrapper.State.FORWARD)).awaitWithTimeout(1000) ?: let {
                HealthCheckData.ServerInfo(
                    null,
                    server.serverInfo.name,
                    false,
                    DatabaseMethod.SQL,
                    SQLConfig.SQLMethod.SQLITE,
                    System.currentTimeMillis()
                )
            }).apply { this.name = server.serverInfo.name })
        }

        return CompletableDeferred(HealthCheckData(proxyInfo, servers))
    }
}