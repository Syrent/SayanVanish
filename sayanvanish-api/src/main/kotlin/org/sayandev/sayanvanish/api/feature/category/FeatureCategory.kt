package org.sayandev.sayanvanish.api.feature.category

import org.sayandev.stickynote.lib.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
interface FeatureCategory {

    val directory: String?
}