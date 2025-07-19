package org.sayandev.sayanvanish.bungeecord.feature.features

import kotlinx.coroutines.delay
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.bungeecord.api.SayanVanishBungeeAPI
import org.sayandev.sayanvanish.bungeecord.event.BungeeUserUnVanishEvent
import org.sayandev.sayanvanish.bungeecord.event.BungeeUserVanishEvent
import org.sayandev.sayanvanish.bungeecord.feature.ListenedFeature
import org.sayandev.stickynote.bungeecord.StickyNote
import org.sayandev.stickynote.bungeecord.launch
import org.sayandev.stickynote.bungeecord.plugin
import kotlinx.serialization.Serializable
import java.util.*
import java.util.concurrent.TimeUnit

@RegisteredFeature
@Serializable
class FeatureSyncEvents(
    // TODO: previous time was 50millis, think of something to ignore/reset previous value
    val checkPeriodMillis: Long = 1000L
) : ListenedFeature("sync_events") {

    @Transient val previousUsers = mutableMapOf<UUID, Boolean>()

    override fun enable() {
        launch {
            delay(checkPeriodMillis)
            while (isActive()) {
                for (user in SayanVanishBungeeAPI.getDatabase().getVanishUsers().await()) {
                    if (previousUsers[user.uniqueId] == null) {
                        previousUsers[user.uniqueId] = user.isVanished
                        continue
                    }

                    if (previousUsers[user.uniqueId] == false && user.isVanished) {
                        previousUsers[user.uniqueId] = true
                        plugin.proxy.pluginManager.callEvent(BungeeUserVanishEvent(SayanVanishBungeeAPI.adapt(user), user.currentOptions))
                    }

                    if (previousUsers[user.uniqueId] == true && !user.isVanished) {
                        previousUsers[user.uniqueId] = false
                        plugin.proxy.pluginManager.callEvent(BungeeUserUnVanishEvent(SayanVanishBungeeAPI.adapt(user), user.currentOptions))
                    }
                }
                delay(checkPeriodMillis)
            }
        }
        super.enable()
    }
}