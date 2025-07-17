package org.sayandev.sayanvanish.bukkit

import org.sayandev.sayanvanish.api.Platform
import org.sayandev.sayanvanish.bukkit.api.BukkitPlatformAdapter
import org.sayandev.sayanvanish.bukkit.config.SettingsConfig
import org.sayandev.stickynote.bukkit.plugin
import org.sayandev.stickynote.bukkit.pluginDirectory

class BukkitPlatform : Platform(
    "bukkit",
    plugin.name,
    plugin.logger,
    pluginDirectory,
    SettingsConfig.get().general.serverId,
    BukkitPlatformAdapter
)