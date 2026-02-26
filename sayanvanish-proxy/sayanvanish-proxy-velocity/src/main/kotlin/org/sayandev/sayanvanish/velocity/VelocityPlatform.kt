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
package org.sayandev.sayanvanish.velocity

import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.sayandev.sayanvanish.api.Platform
import org.sayandev.sayanvanish.api.feature.Feature
import org.sayandev.sayanvanish.velocity.feature.features.FeatureSyncEvents
import org.sayandev.sayanvanish.velocity.feature.features.FeatureUpdate
import org.sayandev.sayanvanish.velocity.feature.features.FeatureUpdatePing
import org.sayandev.sayanvanish.velocity.feature.features.hook.*
import org.sayandev.sayanvanish.velocity.feature.features.prevent.FeaturePreventChat
import org.sayandev.sayanvanish.velocity.feature.features.prevent.FeaturePreventTabComplete
import org.sayandev.stickynote.velocity.StickyNote

class VelocityPlatform : Platform(
    "bukkit",
    "sayanvanish",
    java.util.logging.Logger.getLogger("sayanvanish"),
    StickyNote.dataDirectory.toFile(),
    "",
    VelocityPlatformAdapter,
    SerializersModule {
        fun PolymorphicModuleBuilder<Feature>.registerProjectSubclasses() {
            /* Hooks */
            subclass(FeatureHookAdvancedServerList::class)
            subclass(FeatureHookEnhancedVelocity::class)
            subclass(FeatureHookMiniPlaceholders::class)
            subclass(FeatureHookTAB::class)
            subclass(FeatureHookVelocitab::class)
            subclass(FeatureHookLuckPerms::class)

            /* Prevention */
            subclass(FeaturePreventChat::class)
            subclass(FeaturePreventTabComplete::class)

            /* General */
            subclass(FeatureSyncEvents::class)
            subclass(FeatureUpdate::class)
            subclass(FeatureUpdatePing::class)
        }
        polymorphic(Feature::class) {
            registerProjectSubclasses()
        }
    }
)