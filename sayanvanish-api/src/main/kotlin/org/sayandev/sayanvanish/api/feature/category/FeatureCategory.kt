package org.sayandev.sayanvanish.api.feature.category

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
interface FeatureCategory {

    val directory: String?
}