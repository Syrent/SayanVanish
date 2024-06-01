package org.sayandev

import org.gradle.kotlin.dsl.PluginDependenciesSpecScope
import org.gradle.kotlin.dsl.version

data class Plugin(
    val id: String,
    val version: String?,
    val modules: List<Module>
)

fun PluginDependenciesSpecScope.applyPlugins(module: Module) {
    plugins.filter { plugin -> plugin.modules.contains(module) }.forEach { plugin ->
        if (plugin.version != null) {
            id(plugin.id) version plugin.version
        } else {
            id(plugin.id)
        }
    }
}