package org.sayandev.sayanvanish.bungeecord.event

import net.md_5.bungee.api.plugin.Cancellable
import net.md_5.bungee.api.plugin.Event
import org.sayandev.sayanvanish.api.VanishOptions
import org.sayandev.sayanvanish.bungeecord.api.BungeeUser

class BungeeUserUnVanishEvent(val user: BungeeUser, val options: VanishOptions): Event(), Cancellable {
    private var cancelled = false

    override fun isCancelled(): Boolean {
        return cancelled
    }

    override fun setCancelled(cancelled: Boolean) {
        this.cancelled = cancelled
    }
}