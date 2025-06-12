package org.sayandev.sayanvanish.velocity.feature.features

import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.sayandev.sayanvanish.api.VanishAPI
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.velocity.VelocityPlatformAdapter
import org.sayandev.sayanvanish.velocity.api.SayanVanishVelocityAPI
import org.sayandev.sayanvanish.velocity.api.VelocityVanishUser.Companion.adapt
import org.sayandev.sayanvanish.velocity.event.VelocityUserUnVanishEvent
import org.sayandev.sayanvanish.velocity.event.VelocityUserVanishEvent
import org.sayandev.sayanvanish.velocity.feature.ListenedFeature
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.sayandev.stickynote.velocity.StickyNote
import org.sayandev.stickynote.velocity.launch
import org.sayandev.stickynote.velocity.plugin
import org.sayandev.stickynote.velocity.server
import org.spongepowered.configurate.objectmapping.meta.Comment
import java.util.UUID
import java.util.concurrent.TimeUnit

@RegisteredFeature
@ConfigSerializable
class FeatureSyncEvents(
    @Comment("The period of time to check for vanished players. low values may cause performance issues.")
    val checkPeriodMillis: Long = 50
) : ListenedFeature("sync_events") {

    @Transient val previousUsers = mutableMapOf<UUID, Boolean>()

    override fun enable() {
        launch {
            // TODO: rewrite this so it uses messaging to call events
            while (isActive) {
                for (vanishUser in VanishAPI.get().getDatabase().getVanishUsers().await()) {
                    if (previousUsers[vanishUser.uniqueId] == null) {
                        previousUsers[vanishUser.uniqueId] = vanishUser.isVanished
                        continue
                    }

                    if (previousUsers[vanishUser.uniqueId] == false && vanishUser.isVanished) {
                        previousUsers[vanishUser.uniqueId] = true
                        server.eventManager.fireAndForget(VelocityUserVanishEvent(vanishUser.adapt(), vanishUser.currentOptions))
                    }

                    if (previousUsers[vanishUser.uniqueId] == true && !vanishUser.isVanished) {
                        previousUsers[vanishUser.uniqueId] = false
                        server.eventManager.fireAndForget(VelocityUserUnVanishEvent(vanishUser.adapt(), vanishUser.currentOptions))
                    }
                }
                delay(checkPeriodMillis)
            }
        }
        super.enable()
    }
}