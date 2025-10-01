package org.sayandev.sayanvanish.velocity.feature.features

import com.charleskorn.kaml.YamlComment
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.velocity.feature.ListenedFeature
import java.util.*

// TODO: new event implementation using messaging and custom api
@RegisteredFeature
@Serializable
@SerialName("sync_events")
class FeatureSyncEvents(
    @YamlComment("The period of time to check for vanished players. low values may cause performance issues.")
    val checkPeriodMillis: Long = 50
) : ListenedFeature() {

    @Transient override val id = "sync_events"

    @Transient val previousUsers = mutableMapOf<UUID, Boolean>()

    override fun enable() {
        /*launch {
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
        }*/
        super.enable()
    }
}