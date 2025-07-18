package org.sayandev.sayanvanish.bukkit.feature.features

import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import net.kyori.adventure.text.Component
import org.bukkit.event.EventHandler
import org.sayandev.sayanvanish.api.Permission
import org.sayandev.sayanvanish.api.VanishAPI
import org.sayandev.sayanvanish.api.feature.Configurable
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.cachedVanishUser
import org.sayandev.sayanvanish.bukkit.api.event.BukkitUserUnVanishEvent
import org.sayandev.sayanvanish.bukkit.api.event.BukkitUserVanishEvent
import org.sayandev.sayanvanish.bukkit.feature.ListenedFeature
import org.sayandev.stickynote.bukkit.StickyNote
import org.sayandev.stickynote.bukkit.launch
import org.sayandev.stickynote.bukkit.onlinePlayers
import org.sayandev.stickynote.bukkit.utils.AdventureUtils.component
import org.sayandev.stickynote.bukkit.warn
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment

@RegisteredFeature
@ConfigSerializable
class FeatureActionbar(
    @Comment("The content of the actionbar message.")
    @Configurable val content: String = "<gray>You are currently vanished!",
    @Comment("The delay before the actionbar message is sent. doesn't really matter.")
    @Configurable val delayMillis: Long = 1000,
    @Comment("The period between each actionbar message. values higher than 40 will make it not always visible.")
    @Configurable val periodMillis: Long = 1000,
) : ListenedFeature("actionbar") {

    @EventHandler
    private fun onVanish(event: BukkitUserVanishEvent) {
        val user = event.user
        if (!isActive(user)) return
        user.sendActionbar(content.component())
    }

    @EventHandler
    private fun onUnVanish(event: BukkitUserUnVanishEvent) {
        val user = event.user
        if (!isActive(user)) return
        user.sendActionbar(Component.empty())
    }

    override fun enable() {
        launch {
            delay(delayMillis)
            while (StickyNote.plugin().isEnabled && enabled && isActive) {
                warn("vanished users: ${VanishAPI.get().getCacheService().getVanishUsers().values.joinToString(" ,") { "${it.username}:${it.isVanished}" }}")
                for (user in onlinePlayers.mapNotNull { it.cachedVanishUser() }.filter { it.isVanished }) {
                    if (!isActive(user)) continue
                    user.sendActionbar(content.component())
                }
                delay(periodMillis)
            }
        }
        super.enable()
    }

}