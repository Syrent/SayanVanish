package org.sayandev

import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.kotlin.dsl.maven

data class Repository(
    val id: String,
    val repos: List<String>,
    val dependencies: List<Dependency>
)

fun getRepositories(module: Module): List<Repository> {
    return repositories.filter { repository -> repository.dependencies.any { dependency -> dependency.modules.contains(module) } }
}

fun getDependencies(module: Module): List<Dependency> {
    return repositories.flatMap { repository -> repository.dependencies }.filter { dependency -> dependency.modules.contains(module) }
}

fun RepositoryHandler.applyRepositories(module: Module) {
    getRepositories(module).forEach { repository ->
        repository.repos.forEach { repo ->
            this.maven(repo)
        }
    }
}

fun DependencyHandler.applyDependencies(module: Module, shade: Boolean = true) {
    getDependencies(module).forEach { dependency ->
        this.add(if (shade) dependency.type.configurationName else "compileOnly", "${dependency.group}:${dependency.artifact}:${dependency.version}")
    }
}

fun Project.generateRepositoriesClass(module: Module) {
    val file = this.file("src/main/resources/repositories.yml")
    file.parentFile.mkdirs()
    val repositories = org.sayandev.getRepositories(module)
    val dependencies = repositories
        .map { it.dependencies }
        .flatten()
        .filter { it.modules.contains(module) && it.shadeMethod != Dependency.ShadeMethod.FORCE && (it.type == Dependency.Type.IMPLEMENTATION || it.type == Dependency.Type.API) }

    val serializedDependency = dependencies
        .joinToString("\n") { dependency ->
            "  - \"${dependency.group.replace(".", "{}")}:${dependency.artifact}:${dependency.version}" + if (dependency.relocation != null) ":${dependency.relocation.from.replace(".", "{}")}:${dependency.relocation.to.replace(".", "{}")}\"" else "\""
        }
    file.writeText(
        """
            |repositories:
            |${org.sayandev.repositories.map { it.repos }.flatten().joinToString("\n") { "  - $it" } }
            |dependencies:
            |${serializedDependency}
        """.trimMargin()
    )
}

fun Project.generateTemplate(module: Module) {
    val file = this.file("src/main/kotlin/${rootProject.group.toString().replace(".", "/")}/${rootProject.name.lowercase()}/BuildConstants.kt")
    file.parentFile.mkdirs()
    file.writeText(
        """
            |package ${rootProject.group}.${rootProject.name.lowercase()};
            |
            |class BuildConstants {
            |   companion object {
            |       const val STICKYNOTE_VERSION = "$stickynoteVersion"
            |   }
            |}
        """.trimMargin()
    )
}