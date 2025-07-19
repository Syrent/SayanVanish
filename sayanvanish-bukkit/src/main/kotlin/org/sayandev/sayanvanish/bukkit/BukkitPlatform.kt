package org.sayandev.sayanvanish.bukkit

import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.sayandev.sayanvanish.api.Platform
import org.sayandev.sayanvanish.api.feature.Feature
import org.sayandev.sayanvanish.bukkit.api.BukkitPlatformAdapter
import org.sayandev.sayanvanish.bukkit.config.Settings
import org.sayandev.sayanvanish.bukkit.feature.HookFeature
import org.sayandev.sayanvanish.bukkit.feature.ListenedFeature
import org.sayandev.sayanvanish.bukkit.feature.features.FeatureActionbar
import org.sayandev.sayanvanish.bukkit.feature.features.FeatureEffect
import org.sayandev.sayanvanish.bukkit.feature.features.FeatureFakeMessage
import org.sayandev.sayanvanish.bukkit.feature.features.FeatureFly
import org.sayandev.sayanvanish.bukkit.feature.features.FeatureGameMode
import org.sayandev.sayanvanish.bukkit.feature.features.FeatureInventoryInspect
import org.sayandev.sayanvanish.bukkit.feature.features.FeatureInvulnerability
import org.sayandev.sayanvanish.bukkit.feature.features.FeatureLevel
import org.sayandev.sayanvanish.bukkit.feature.features.FeatureRideEntity
import org.sayandev.sayanvanish.bukkit.feature.features.FeatureSilentContainer
import org.sayandev.sayanvanish.bukkit.feature.features.FeatureState
import org.sayandev.sayanvanish.bukkit.feature.features.FeatureUpdate
import org.sayandev.sayanvanish.bukkit.feature.features.hook.*
import org.sayandev.sayanvanish.bukkit.feature.features.prevent.FeatureLegacyPreventPickup
import org.sayandev.sayanvanish.bukkit.feature.features.prevent.FeaturePreventAdvancementAnnounce
import org.sayandev.sayanvanish.bukkit.feature.features.prevent.FeaturePreventBlockBreak
import org.sayandev.sayanvanish.bukkit.feature.features.prevent.FeaturePreventBlockGrief
import org.sayandev.sayanvanish.bukkit.feature.features.prevent.FeaturePreventBlockPlace
import org.sayandev.sayanvanish.bukkit.feature.features.prevent.FeaturePreventChat
import org.sayandev.sayanvanish.bukkit.feature.features.prevent.FeaturePreventCreatureTarget
import org.sayandev.sayanvanish.bukkit.feature.features.prevent.FeaturePreventDamage
import org.sayandev.sayanvanish.bukkit.feature.features.prevent.FeaturePreventFoodLevelChange
import org.sayandev.sayanvanish.bukkit.feature.features.prevent.FeaturePreventInteract
import org.sayandev.sayanvanish.bukkit.feature.features.prevent.FeaturePreventPickup
import org.sayandev.sayanvanish.bukkit.feature.features.prevent.FeaturePreventPush
import org.sayandev.sayanvanish.bukkit.feature.features.prevent.FeaturePreventRaidTrigger
import org.sayandev.sayanvanish.bukkit.feature.features.prevent.FeaturePreventSculk
import org.sayandev.sayanvanish.bukkit.feature.features.prevent.FeaturePreventServerPing
import org.sayandev.sayanvanish.bukkit.feature.features.prevent.FeaturePreventSpawnerSpawn
import org.sayandev.sayanvanish.bukkit.feature.features.prevent.FeaturePreventTabComplete
import org.sayandev.stickynote.bukkit.plugin
import org.sayandev.stickynote.bukkit.pluginDirectory

class BukkitPlatform : Platform(
    "bukkit",
    plugin.name,
    plugin.logger,
    pluginDirectory,
    Settings.get().general.serverId,
    BukkitPlatformAdapter,
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
            subclass(FeatureUpdate::class)
        }
        polymorphic(Feature::class) {
            registerProjectSubclasses()
        }
    }
)