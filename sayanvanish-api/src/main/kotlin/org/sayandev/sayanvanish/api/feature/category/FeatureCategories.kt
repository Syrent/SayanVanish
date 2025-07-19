package org.sayandev.sayanvanish.api.feature.category

import kotlinx.serialization.Serializable

@Serializable
enum class FeatureCategories(override val directory: String?) : FeatureCategory {
    HOOK("hooks"),
    PREVENTION("preventions"),
    PROXY("proxy"),
    DEFAULT(null),
    CUSTOM("custom")
}