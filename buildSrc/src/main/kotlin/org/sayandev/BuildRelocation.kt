package org.sayandev

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy

fun getProjectRelocations(): List<BuildRelocation> {
    return repositories.map { it.dependencies }
        .flatten()
        .filter { it.relocation != null }
        .map { BuildRelocation(it.relocation!!.from, it.relocation.to, it.modules) }
}

fun Project.getRelocations(): List<BuildRelocation> = getProjectRelocations()

fun getProjectRelocations(module: Module): List<BuildRelocation> {
    return getProjectRelocations().filter { it.relocateModules.contains(module) }
}

fun Project.getRelocations(module: Module): List<BuildRelocation> = getProjectRelocations(module)

fun ShadowJar.applyShadowRelocation(module: Module) {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    getProjectRelocations(module).forEach { relocate ->
        relocate(relocate.from, relocate.to)
    }
}

data class BuildRelocation(
    val from: String,
    val to: String,
    val relocateModules: List<Module>
)