import org.sayandev.*
import org.sayandev.applyShadowRelocation

plugins {
    id("net.minecrell.plugin-yml.bungee") version "0.6.0"
}

generateTemplate(Module.BUNGEECORD)

repositories {
    applyRepositories(Module.BUNGEECORD)
}

dependencies {
    applyDependencies(Module.BUNGEECORD)
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