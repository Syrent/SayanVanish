package org.sayandev.sayanvanish.api.health

import org.sayandev.sayanvanish.api.storage.DatabaseType
import org.sayandev.sayanvanish.api.storage.sql.SQLConfig

data class HealthCheckData(
    val proxy: ProxyInfo,
    val servers: Servers,
) {

    data class ProxyInfo(
        val databaseType: DatabaseType,
        val sqlMethod: SQLConfig.SQLMethod,
    )

    data class ServerInfo(
        val id: String?,
        var name: String?,
        val enabledProxyMode: Boolean,
        val databaseType: DatabaseType,
        val sqlMethod: SQLConfig.SQLMethod,
        val timestamp: Long
    )

    data class Servers(
        val servers: MutableList<ServerInfo>
    )

}