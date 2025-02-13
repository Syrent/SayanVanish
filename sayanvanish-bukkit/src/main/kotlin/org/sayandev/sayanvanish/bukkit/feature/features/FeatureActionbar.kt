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
import org.spongepowered.configurate.objectmapping.meta.Comment

@RegisteredFeature
@ConfigSerializable
class FeatureActionbar(
    @Comment("The content of the actionbar message.")
    @Configurable val content: String = "<gray>You are currently vanished!",
    @Comment("The delay before the actionbar message is sent. doesn't really matter.")
    @Configurable val delay: Long = 20,
    @Comment("The period between each actionbar message. values higher than 40 will make it not always visible.")
    @Configurable val period: Long = 20,
) : ListenedFeature("actionbar") {

    @EventHandler
    private fun onVanish(event: BukkitUserVanishEvent) {
        val user = event.user
        if (!isActive(user)) return
        user.sendActionbar(content)
    }

    @EventHandler
    private fun onUnVanish(event: BukkitUserUnVanishEvent) {
        val user = event.user
        if (!isActive(user)) return
        user.sendActionbar("")
    }

    override fun enable() {
        runSync({
            for (user in onlinePlayers.filter { it.hasPermission(Permission.VANISH.permission()) }.mapNotNull { it.user() }.filter { it.isVanished }) {
                if (!isActive(user)) continue
                user.sendActionbar(content)
            }
        }, delay, period)
        super.enable()
    }

}