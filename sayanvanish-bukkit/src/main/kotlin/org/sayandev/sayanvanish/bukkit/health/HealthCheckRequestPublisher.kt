package org.sayandev.sayanvanish.bukkit.health

import org.sayandev.sayanvanish.api.health.HealthCheckData
import org.sayandev.stickynote.bukkit.messaging.publisher.ProxyPluginMessagePublisher
import org.sayandev.stickynote.bukkit.plugin

object HealthCheckRequestPublisher : ProxyPluginMessagePublisher<Unit, HealthCheckData>(
    plugin.name.lowercase(),
    "healthcheck",
    Unit::class.java,
    HealthCheckData::class.java
)