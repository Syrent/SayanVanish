package org.sayandev.sayanvanish.api.feature

import org.reflections.Reflections
import org.sayandev.sayanvanish.api.Platform
import java.io.IOException
import java.net.URL
import java.security.CodeSource
import java.util.*
import java.util.jar.JarEntry
import java.util.jar.JarFile
import kotlin.collections.ArrayList


object RegisteredFeatureHandler {

    fun process() {
        val reflections = Reflections("org.sayandev.sayanvanish")
        val annotatedClasses = if (reflections.getTypesAnnotatedWith(RegisteredFeature::class.java).isEmpty()) {
            Platform.get().logger.warning("Couldn't load plugin features in your current server software, trying alternative method...")
            getClassesInPackage(Platform.get(), "org.sayandev.sayanvanish")
        } else {
            reflections.getTypesAnnotatedWith(RegisteredFeature::class.java)
        }

        Platform.get().logger.info("Found ${annotatedClasses.size} features.")
        for (annotatedClass in annotatedClasses) {
            createNewInstance(annotatedClass)
        }
        Platform.get().logger.info("Enabled ${Features.features.filter { it.isActive() }.size} features.")
    }

    private fun createNewInstance(clazz: Class<*>) {
        try {
            if (Features.features.map { it.javaClass }.contains(clazz)) return
            val instance = Feature.createFromConfig(clazz as Class<out Feature>)
            instance.load()
            when (instance) {
                is Feature -> {
                    Features.addFeature(instance)
                }
                else -> {
                    throw NullPointerException("Tried to add feature to Features but the type ${clazz.name} is not supported")
                }
            }
        } catch (e: NoClassDefFoundError) {
            Platform.get().logger.warning("Couldn't enable feature ${clazz.simpleName} on your server software/version.")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

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
                        classes.add(clazz)
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