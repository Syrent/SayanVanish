package org.sayandev.sayanvanish.api.feature

import org.reflections.Reflections
import org.sayandev.sayanvanish.api.Platform

interface FeatureDiscoveryStrategy {
    fun discover(platform: Platform): Collection<Class<out Feature>>
}

object DefaultFeatureDiscoveryStrategy : FeatureDiscoveryStrategy {
    override fun discover(platform: Platform): Collection<Class<out Feature>> {
        val discovered = linkedSetOf<Class<out Feature>>()
        val packages = platform.featureScanPackages.ifEmpty { setOf("org.sayandev.sayanvanish") }

        for (packageName in packages) {
            val reflections = Reflections(packageName)
            val reflectionResults = reflections
                .getTypesAnnotatedWith(RegisteredFeature::class.java)
                .mapNotNull { it as? Class<out Feature> }

            if (reflectionResults.isNotEmpty()) {
                discovered.addAll(reflectionResults)
                continue
            }

            discovered.addAll(
                RegisteredFeatureHandler
                    .getClassesInPackage(platform, packageName)
                    .mapNotNull { it as? Class<out Feature> }
            )
        }

        return discovered
    }
}
