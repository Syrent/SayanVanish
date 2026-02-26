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
package org.sayandev.sayanvanish.paper

import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.sayandev.sayanvanish.api.Platform
import org.sayandev.sayanvanish.api.feature.Feature
import org.sayandev.sayanvanish.paper.api.PaperPlatformAdapter
import org.sayandev.sayanvanish.paper.config.Settings
import org.sayandev.sayanvanish.paper.feature.features.FeatureActionbar
import org.sayandev.sayanvanish.paper.feature.features.FeatureEffect
import org.sayandev.sayanvanish.paper.feature.features.FeatureFakeMessage
import org.sayandev.sayanvanish.paper.feature.features.FeatureFly
import org.sayandev.sayanvanish.paper.feature.features.FeatureGameMode
import org.sayandev.sayanvanish.paper.feature.features.FeatureInventoryInspect
import org.sayandev.sayanvanish.paper.feature.features.FeatureInvulnerability
import org.sayandev.sayanvanish.paper.feature.features.FeatureLevel
import org.sayandev.sayanvanish.paper.feature.features.FeatureRegisterPermissions
import org.sayandev.sayanvanish.paper.feature.features.FeatureRideEntity
import org.sayandev.sayanvanish.paper.feature.features.FeatureSilentContainer
import org.sayandev.sayanvanish.paper.feature.features.FeatureState
import org.sayandev.sayanvanish.paper.feature.features.FeatureUpdate
import org.sayandev.sayanvanish.paper.feature.features.hook.*
import org.sayandev.sayanvanish.paper.feature.features.prevent.FeatureLegacyPreventPickup
import org.sayandev.sayanvanish.paper.feature.features.prevent.FeaturePreventAdvancementAnnounce
import org.sayandev.sayanvanish.paper.feature.features.prevent.FeaturePreventBlockBreak
import org.sayandev.sayanvanish.paper.feature.features.prevent.FeaturePreventBlockGrief
import org.sayandev.sayanvanish.paper.feature.features.prevent.FeaturePreventBlockPlace
import org.sayandev.sayanvanish.paper.feature.features.prevent.FeaturePreventChat
import org.sayandev.sayanvanish.paper.feature.features.prevent.FeaturePreventCreatureTarget
import org.sayandev.sayanvanish.paper.feature.features.prevent.FeaturePreventDamage
import org.sayandev.sayanvanish.paper.feature.features.prevent.FeaturePreventFoodLevelChange
import org.sayandev.sayanvanish.paper.feature.features.prevent.FeaturePreventInteract
import org.sayandev.sayanvanish.paper.feature.features.prevent.FeaturePreventPickup
import org.sayandev.sayanvanish.paper.feature.features.prevent.FeaturePreventPush
import org.sayandev.sayanvanish.paper.feature.features.prevent.FeaturePreventRaidTrigger
import org.sayandev.sayanvanish.paper.feature.features.prevent.FeaturePreventSculk
import org.sayandev.sayanvanish.paper.feature.features.prevent.FeaturePreventServerPing
import org.sayandev.sayanvanish.paper.feature.features.prevent.FeaturePreventSpawnerSpawn
import org.sayandev.sayanvanish.paper.feature.features.prevent.FeaturePreventTabComplete
import org.sayandev.stickynote.bukkit.plugin
import org.sayandev.stickynote.bukkit.pluginDirectory

class PaperPlatform : Platform(
    "bukkit",
    plugin.name,
    plugin.logger,
    pluginDirectory,
    Settings.get().general.serverId,
    PaperPlatformAdapter,
    SerializersModule {
        fun PolymorphicModuleBuilder<Feature>.registerProjectSubclasses() {
            /* Hooks */
            subclass(FeatureHookAdvancedServerList::class)
            subclass(FeatureHookCitizens::class)
            subclass(FeatureHookDiscordSRV::class)
            subclass(FeatureHookDynmap::class)
            subclass(FeatureHookEssentials::class)
            subclass(FeatureHookLuckPerms::class)
            subclass(FeatureHookMiniPlaceholders::class)
            subclass(FeatureHookPl3xMap::class)
            subclass(FeatureHookPlaceholderAPI::class)
            subclass(FeatureHookSquareMap::class)
            subclass(FeatureHookTAB::class)

            /* Prevention */
            subclass(FeaturePreventAdvancementAnnounce::class)
            subclass(FeaturePreventBlockBreak::class)
            subclass(FeaturePreventBlockGrief::class)
            subclass(FeaturePreventBlockPlace::class)
            subclass(FeaturePreventChat::class)
            subclass(FeaturePreventCreatureTarget::class)
            subclass(FeaturePreventDamage::class)
            subclass(FeaturePreventFoodLevelChange::class)
            subclass(FeaturePreventInteract::class)
            subclass(FeaturePreventPickup::class)
            subclass(FeatureLegacyPreventPickup::class)
            subclass(FeaturePreventPush::class)
            subclass(FeaturePreventRaidTrigger::class)
            subclass(FeaturePreventSculk::class)
            subclass(FeaturePreventServerPing::class)
            subclass(FeaturePreventSpawnerSpawn::class)
            subclass(FeaturePreventTabComplete::class)

            /* General */
            subclass(FeatureActionbar::class)
            subclass(FeatureEffect::class)
            subclass(FeatureFakeMessage::class)
            subclass(FeatureFly::class)
            subclass(FeatureGameMode::class)
            subclass(FeatureInventoryInspect::class)
            subclass(FeatureInvulnerability::class)
            subclass(FeatureLevel::class)
            subclass(FeatureRideEntity::class)
            subclass(FeatureSilentContainer::class)
            subclass(FeatureState::class)
            subclass(FeatureRegisterPermissions::class)
            subclass(FeatureUpdate::class)
        }
        polymorphic(Feature::class) {
            registerProjectSubclasses()
        }
    }
)