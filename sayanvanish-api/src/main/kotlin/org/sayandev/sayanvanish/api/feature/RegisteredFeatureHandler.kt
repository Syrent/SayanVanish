package org.sayandev.sayanvanish.api.feature

import org.sayandev.stickynote.lib.reflections.Reflections


object RegisteredFeatureHandler {

    fun process() {
        val reflections = Reflections("org.sayandev.sayanvanish")
        val annotatedClasses = reflections.getTypesAnnotatedWith(RegisteredFeature::class.java)

        for (annotatedClass in annotatedClasses) {
            createNewInstance(annotatedClass)
        }
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
                    throw NullPointerException("Tried to add item to Items but the type ${clazz.name} is not supported")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}