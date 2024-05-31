package org.sayandev.sayanvanish.bukkit.feature.features

import org.bukkit.event.EventHandler
import org.sayandev.sayanvanish.api.Permission
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.user
import org.sayandev.sayanvanish.bukkit.api.event.BukkitUserUnVanishEvent
import org.sayandev.sayanvanish.bukkit.api.event.BukkitUserVanishEvent
import org.sayandev.sayanvanish.bukkit.feature.ListenedFeature
import org.sayandev.stickynote.bukkit.StickyNote.runSync
import org.sayandev.stickynote.bukkit.onlinePlayers
import org.sayandev.stickynote.bukkit.utils.AdventureUtils.component
import org.sayandev.stickynote.lib.kyori.adventure.text.Component
import org.sayandev.stickynote.lib.spongepowered.configurate.objectmapping.ConfigSerializable

@RegisteredFeature
@ConfigSerializable
class FeatureActionbar(
    val content: String = "<gray>You are currenly vanished!",
    val repeatEvery: Long = 20,
) : ListenedFeature("actionbar") {

    @EventHandler
    private fun onVanish(event: BukkitUserVanishEvent) {
        if (!isActive()) return
        val user = event.user
        user.sendActionbar(content.component())
    }

    @EventHandler
    private fun onUnVanish(event: BukkitUserUnVanishEvent) {
        if (!isActive()) return
        val user = event.user
        user.sendActionbar(Component.empty())
    }

    override fun enable() {
        runSync({
            if (!enabled) return@runSync
            for (user in onlinePlayers.filter { it.hasPermission(Permission.VANISH.permission()) }.mapNotNull { it.user() }.filter { it.isVanished }) {
                user.sendActionbar(content.component())
            }
        }, repeatEvery, repeatEvery)
        super.enable()
    }

}