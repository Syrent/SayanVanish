package org.sayandev.sayanvanish.api.feature

import org.sayandev.sayanvanish.api.BasicUser
import java.util.UUID

object Features {
    val features = mutableListOf<Feature>()
    val userFeatures = mutableMapOf<UUID, List<Feature>>()

    @JvmStatic
    inline fun <reified T> getFeature(): T {
        return features.find { it is T } as T
    }

    @JvmStatic
    inline fun <reified T> getUserFeature(uniqueId: UUID): T {
        return userFeatures[uniqueId] as T
    }

    @JvmStatic
    inline fun <reified T> getUserFeature(user: BasicUser): T {
        return getUserFeature(user.uniqueId)
    }

    @JvmStatic
    fun addFeature(feature: Feature) {
        features.add(feature)
    }

    @JvmStatic
    fun features(): List<Feature> {
        return features
    }

    fun userFeatures(user: BasicUser): List<Feature> {
        return userFeatures.getOrPut(user.uniqueId) { features() }
    }

}