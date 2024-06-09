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