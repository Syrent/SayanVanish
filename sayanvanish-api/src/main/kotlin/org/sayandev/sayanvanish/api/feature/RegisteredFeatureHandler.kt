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
