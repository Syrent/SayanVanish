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