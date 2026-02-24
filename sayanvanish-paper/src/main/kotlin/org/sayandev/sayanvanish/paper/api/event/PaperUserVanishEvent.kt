package org.sayandev.sayanvanish.paper.api.event

import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.sayandev.sayanvanish.api.VanishOptions
import org.sayandev.sayanvanish.paper.api.PaperVanishUser

class PaperUserVanishEvent(
    val user: PaperVanishUser,
    val options: VanishOptions,
) : Event(), Cancellable {

    private var cancelled = false

    override fun getHandlers(): HandlerList {
        return HANDLERS
    }

    companion object {
        @JvmStatic
        private val HANDLERS: HandlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return HANDLERS
        }
    }

    override fun isCancelled(): Boolean {
        return cancelled
    }

    override fun setCancelled(cancel: Boolean) {
        this.cancelled = cancel
    }
}