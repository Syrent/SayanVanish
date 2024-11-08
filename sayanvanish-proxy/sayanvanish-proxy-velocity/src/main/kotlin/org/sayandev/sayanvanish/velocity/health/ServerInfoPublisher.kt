package org.sayandev.sayanvanish.velocity.health

import org.sayandev.sayanvanish.api.health.HealthCheckData
import org.sayandev.stickynote.velocity.messaging.PluginMessagePublisher
import org.sayandev.stickynote.velocity.plugin

object ServerInfoPublisher: PluginMessagePublisher<Unit, HealthCheckData.ServerInfo>(
    plugin.container.description.name.get().lowercase(),
    "info",
    HealthCheckData.ServerInfo::class.java
) {
    init {
        register(this)
    }

    override fun handle(payload: Unit): HealthCheckData.ServerInfo? {
        return null
    }
}