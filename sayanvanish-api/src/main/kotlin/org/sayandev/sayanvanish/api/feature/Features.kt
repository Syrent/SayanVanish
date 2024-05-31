package org.sayandev.sayanvanish.api.feature

object Features {
    val features = mutableListOf<Feature>()

    @JvmStatic
    fun addFeature(feature: Feature) {
        features.add(feature)
    }

    @JvmStatic
    fun features(): List<Feature> {
        return features
    }
}