/*
 * This file is part of SayanVanish, licensed under the GNU General Public License v3.0.
 *
 * Copyright (c) 2026 Sayan Development and contributors
 *
 * SayanVanish is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SayanVanish is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
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
