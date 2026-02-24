package org.sayandev.sayanvanish.api.feature

import org.sayandev.sayanvanish.api.User
import org.sayandev.sayanvanish.api.VanishAPI
import java.util.UUID

object Features {
    private fun registry(): FeatureRegistry {
        return VanishAPI.get().getFeatureRegistry()
    }

    @JvmStatic
    inline fun <reified T : Feature> getFeature(): T {
        return getFeature(T::class.java)
    }

    @JvmStatic
    fun <T : Feature> getFeature(type: Class<T>): T {
        return registry().findByType(type)
            ?: throw IllegalArgumentException("Feature `${type.simpleName}` is not registered.")
    }

    @JvmStatic
    fun getFeatureById(id: String): Feature? {
        return registry().findById(id)
    }

    @JvmStatic
    fun addFeature(feature: Feature): Boolean {
        return registry().add(feature)
    }

    @JvmStatic
    fun removeFeature(feature: Feature): Boolean {
        return registry().remove(feature)
    }

    @JvmStatic
    fun removeFeature(id: String): Boolean {
        return registry().remove(id)
    }

    @JvmStatic
    fun clearFeatures() {
        registry().clear()
    }

    @JvmStatic
    fun features(): List<Feature> {
        return registry().all()
    }

    @JvmStatic
    fun userFeatures(user: User): List<Feature> {
        return features().filter { registry().isEnabledForUser(user, it) }
    }

    @JvmStatic
    fun isFeatureEnabled(user: User, feature: Feature): Boolean {
        return registry().isEnabledForUser(user, feature)
    }

    @JvmStatic
    fun setFeatureEnabled(user: User, feature: Feature, enabled: Boolean) {
        registry().setEnabledForUser(user, feature, enabled)
    }

    @JvmStatic
    fun resetUserFeatureState(uniqueId: UUID) {
        registry().resetUser(uniqueId)
    }

    @JvmStatic
    fun resetUserFeatureState(user: User) {
        resetUserFeatureState(user.uniqueId)
    }

    @JvmStatic
    fun resetAllUserFeatureStates() {
        registry().resetUsers()
    }

}
