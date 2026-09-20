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

import org.sayandev.sayanvanish.api.Platform
import java.io.IOException
import java.net.URL
import java.security.CodeSource
import java.util.*
import java.util.jar.JarEntry
import java.util.jar.JarFile


object RegisteredFeatureHandler {
    @Volatile
    private var discoveryStrategy: FeatureDiscoveryStrategy = DefaultFeatureDiscoveryStrategy

    @Volatile
    private var instantiationStrategy: FeatureInstantiationStrategy = DefaultFeatureInstantiationStrategy

    private val manualFeatureClasses = linkedSetOf<Class<out Feature>>()

    @JvmStatic
    fun setDiscoveryStrategy(strategy: FeatureDiscoveryStrategy) {
        discoveryStrategy = strategy
    }

    @JvmStatic
    fun setInstantiationStrategy(strategy: FeatureInstantiationStrategy) {
        instantiationStrategy = strategy
    }

    @JvmStatic
    fun resetStrategies() {
        discoveryStrategy = DefaultFeatureDiscoveryStrategy
        instantiationStrategy = DefaultFeatureInstantiationStrategy
    }

    @JvmStatic
    fun registerFeatureClass(featureClass: Class<out Feature>) {
        manualFeatureClasses.add(featureClass)
    }

    @JvmStatic
    fun unregisterFeatureClass(featureClass: Class<out Feature>) {
        manualFeatureClasses.remove(featureClass)
    }

    @JvmStatic
    fun clearManualFeatureClasses() {
        manualFeatureClasses.clear()
    }

    @JvmStatic
    fun process() {
        val annotatedClasses = linkedSetOf<Class<out Feature>>()
        annotatedClasses.addAll(discoveryStrategy.discover(Platform.get()))
        annotatedClasses.addAll(manualFeatureClasses)

        Platform.get().logger.info("Found ${annotatedClasses.size} features.")
        for (annotatedClass in annotatedClasses) {
            createNewInstance(annotatedClass)
        }
        Platform.get().logger.info("Enabled ${Features.features().count { it.isActive() }} features.")
    }

    private fun createNewInstance(clazz: Class<out Feature>) {
        try {
            if (Features.features().map { it.javaClass }.contains(clazz)) return
            val instance = instantiationStrategy.create(clazz)
            instance.save()
            Features.addFeature(instance)
        } catch (e: NoClassDefFoundError) {
            Platform.get().logger.warning("Couldn't enable feature ${clazz.simpleName} on your server software/version.")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun getClassesInPackage(plugin: Any, packageName: String): Collection<Class<*>> {
        val classes: MutableCollection<Class<*>> = ArrayList()
        val codeSource: CodeSource = plugin.javaClass.protectionDomain.codeSource
        val resource: URL = codeSource.location
        val relPath = packageName.replace('.', '/')
        val resPath = resource.path.replace("%20", " ")
        val jarPath = resPath.replaceFirst("[.]jar[!].*".toRegex(), ".jar").replaceFirst("file:", "")

        val jarFile: JarFile = try {
            JarFile(jarPath)
        } catch (e: IOException) {
            Platform.get().logger.severe("Tried to find plugin jar file to load features, but couldn't find it. Your server software doesn't support this behavior.")
            return emptyList()
        }

        val entries: Enumeration<JarEntry> = jarFile.entries()

        while (entries.hasMoreElements()) {
            val entry: JarEntry = entries.nextElement()
            val entryName: String = entry.name
            if (entryName.endsWith(".class") && entryName.startsWith(relPath) && entryName.length > relPath.length + 1) {
                val className = entryName.replace('/', '.').replace('\\', '.').replace(".class", "")
                try {
                    val clazz = plugin.javaClass.classLoader.loadClass(className)
                    if (clazz.isAnnotationPresent(RegisteredFeature::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        classes.add(clazz as Class<out Feature>)
                    }
                } catch (_: NoClassDefFoundError) {
                } catch (_: ClassNotFoundException) { }
            }
        }

        try {
            jarFile.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return classes
    }

}
