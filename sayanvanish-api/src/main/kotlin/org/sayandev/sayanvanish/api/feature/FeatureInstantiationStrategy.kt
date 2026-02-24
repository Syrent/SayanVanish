package org.sayandev.sayanvanish.api.feature

interface FeatureInstantiationStrategy {
    fun create(type: Class<out Feature>): Feature
}

object DefaultFeatureInstantiationStrategy : FeatureInstantiationStrategy {
    override fun create(type: Class<out Feature>): Feature {
        return Feature.createFromConfig(type)
    }
}
