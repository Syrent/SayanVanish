package org.sayandev.sayanvanish.velocity.feature.features

import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.velocity.api.SayanVanishVelocityAPI
import org.sayandev.sayanvanish.velocity.event.VelocityUserUnVanishEvent
import org.sayandev.sayanvanish.velocity.event.VelocityUserVanishEvent
import org.sayandev.sayanvanish.velocity.feature.ListenedFeature
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.sayandev.stickynote.velocity.StickyNote
import org.sayandev.stickynote.velocity.server
import org.spongepowered.configurate.objectmapping.meta.Comment
import java.util.UUID
import java.util.concurrent.TimeUnit

@RegisteredFeature
@ConfigSerializable
data class FeatureSyncEvents(
    @Comment("The period of time to check for vanished players. low values may cause performance issues.")
    val checkPeriodMillis: Long = 50
) : ListenedFeature("sync_events") {

    @Transient val previousUsers = mutableMapOf<UUID, Boolean>()

    override fun enable() {
        StickyNote.run({
            for (user in SayanVanishVelocityAPI.getInstance().database.getUsers()) {
                if (previousUsers[user.uniqueId] == null) {
                    previousUsers[user.uniqueId] = user.isVanished
                    continue
                }

                if (previousUsers[user.uniqueId] == false && user.isVanished) {
                    previousUsers[user.uniqueId] = true
                    server.eventManager.fireAndForget(VelocityUserVanishEvent(user, user.currentOptions))
                }

                if (previousUsers[user.uniqueId] == true && !user.isVanished) {
                    previousUsers[user.uniqueId] = false
                    server.eventManager.fireAndForget(VelocityUserUnVanishEvent(user, user.currentOptions))
                }
            }
        }, checkPeriodMillis, TimeUnit.MILLISECONDS, checkPeriodMillis, TimeUnit.MILLISECONDS)
        super.enable()
    }
}