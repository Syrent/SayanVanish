package org.sayandev.sayanvanish.bungeecord.feature.features

import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.bungeecord.api.SayanVanishBungeeAPI
import org.sayandev.sayanvanish.bungeecord.event.BungeeUserUnVanishEvent
import org.sayandev.sayanvanish.bungeecord.feature.ListenedFeature
import org.sayandev.stickynote.bungeecord.StickyNote
import org.sayandev.stickynote.bungeecord.plugin
import org.sayandev.stickynote.lib.spongepowered.configurate.objectmapping.ConfigSerializable
import java.util.*
import java.util.concurrent.TimeUnit

@RegisteredFeature
@ConfigSerializable
class FeatureSyncEvents(
    val checkPeriodMillis: Long = 50
) : ListenedFeature("sync_events") {

    @Transient val previousUsers = mutableMapOf<UUID, Boolean>()

    override fun enable() {
        StickyNote.run({
            for (user in SayanVanishBungeeAPI.getInstance().database.getUsers()) {
                if (previousUsers[user.uniqueId] == null) {
                    previousUsers[user.uniqueId] = user.isVanished
                    continue
                }

                if (previousUsers[user.uniqueId] == false && user.isVanished) {
                    previousUsers[user.uniqueId] = true
                    plugin.proxy.pluginManager.callEvent(BungeeUserUnVanishEvent(user, user.currentOptions))
                }

                if (previousUsers[user.uniqueId] == true && !user.isVanished) {
                    previousUsers[user.uniqueId] = false
                    plugin.proxy.pluginManager.callEvent(BungeeUserUnVanishEvent(user, user.currentOptions))
                }
            }
        }, checkPeriodMillis, checkPeriodMillis, TimeUnit.MILLISECONDS)
        super.enable()
    }
}