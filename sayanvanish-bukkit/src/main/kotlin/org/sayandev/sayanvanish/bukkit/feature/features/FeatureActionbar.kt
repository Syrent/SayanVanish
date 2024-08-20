package org.sayandev.sayanvanish.bukkit.feature.features

import org.bukkit.event.EventHandler
import org.sayandev.sayanvanish.api.Permission
import org.sayandev.sayanvanish.api.feature.Configurable
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.user
import org.sayandev.sayanvanish.bukkit.api.event.BukkitUserUnVanishEvent
import org.sayandev.sayanvanish.bukkit.api.event.BukkitUserVanishEvent
import org.sayandev.sayanvanish.bukkit.feature.ListenedFeature
import org.sayandev.stickynote.bukkit.StickyNote.runSync
import org.sayandev.stickynote.bukkit.onlinePlayers
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@RegisteredFeature
@ConfigSerializable
class FeatureActionbar(
    @Configurable val content: String = "<gray>You are currently vanished!",
    @Configurable val delay: Long = 20,
    @Configurable val period: Long = 20,
) : ListenedFeature("actionbar") {

    @EventHandler
    private fun onVanish(event: BukkitUserVanishEvent) {
        if (!isActive()) return
        val user = event.user
        user.sendActionbar(content)
    }

    @EventHandler
    private fun onUnVanish(event: BukkitUserUnVanishEvent) {
        if (!isActive()) return
        val user = event.user
        user.sendActionbar("")
    }

    override fun enable() {
        runSync({
            if (!enabled) return@runSync
            for (user in onlinePlayers.filter { it.hasPermission(Permission.VANISH.permission()) }.mapNotNull { it.user() }.filter { it.isVanished }) {
                user.sendActionbar(content)
            }
        }, delay, period)
        super.enable()
    }

}