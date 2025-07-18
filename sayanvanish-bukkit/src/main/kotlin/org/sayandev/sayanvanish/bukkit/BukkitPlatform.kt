package org.sayandev.sayanvanish.bukkit

import org.sayandev.sayanvanish.api.Platform
import org.sayandev.sayanvanish.bukkit.api.BukkitPlatformAdapter
import org.sayandev.sayanvanish.bukkit.config.Settings
import org.sayandev.stickynote.bukkit.plugin
import org.sayandev.stickynote.bukkit.pluginDirectory

class BukkitPlatform : Platform(
    "bukkit",
    plugin.name,
    plugin.logger,
    pluginDirectory,
    Settings.get().general.serverId,
    BukkitPlatformAdapter
)