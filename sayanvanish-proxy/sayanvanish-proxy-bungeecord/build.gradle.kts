import org.sayandev.*
import org.sayandev.applyShadowRelocation
import org.sayandev.plugin.StickyNoteModules

plugins {
    id("net.minecrell.plugin-yml.bungee") version "0.6.0"
}

stickynote {
    modules(StickyNoteModules.BUNGEECORD)
}

repositories {
    applyRepositories(Module.BUNGEECORD)
}

dependencies {
    applyDependencies(Module.BUNGEECORD)

    api(project(":sayanvanish-proxy"))
}

modrinth {
    loaders.set(listOf("bungeecord", "waterfall"))
    detectLoaders.set(false)
}

tasks {
    shadowJar {
        applyShadowRelocation(Module.BUNGEECORD)
    }
}

bungee {
    name = rootProject.name
    version = rootProject.version as String
    description = rootProject.description

    main = "${rootProject.group}.${findProperty("slug")!! as String}.bungeecord.${rootProject.name}"

    author = findProperty("author")!! as String
}