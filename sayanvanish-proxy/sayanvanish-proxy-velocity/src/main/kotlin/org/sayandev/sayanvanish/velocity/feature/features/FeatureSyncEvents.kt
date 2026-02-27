/*
 * This file is part of SayanVanish, licensed under the GNU General Public License v3.0.
 *
 * Copyright (c) 2026 Sayan Development and contributors
 *
 * SayanVanish is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SayanVanish is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
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