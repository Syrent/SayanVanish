package org.sayandev.sayanvanish.api.feature

object Features {
    val features = mutableListOf<Feature>()

    @JvmStatic
    inline fun <reified T> getFeature(): T {
        return features.find { it is T } as T
    }

    @JvmStatic
    fun addFeature(feature: Feature) {
        features.add(feature)
    }

    @JvmStatic
    fun features(): List<Feature> {
        return features
    }
}