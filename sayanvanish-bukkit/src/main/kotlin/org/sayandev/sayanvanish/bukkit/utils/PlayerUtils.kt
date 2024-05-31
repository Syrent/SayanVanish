package org.sayandev.sayanvanish.bukkit.utils

import org.bukkit.OfflinePlayer

class PlayerUtils {

    fun OfflinePlayer.isPlayingOnThisServer(): Boolean {
        return this.isOnline
    }

}