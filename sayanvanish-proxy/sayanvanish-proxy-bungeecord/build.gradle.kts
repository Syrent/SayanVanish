import org.sayandev.plugin.StickyNoteModules

plugins {
    id("net.minecrell.plugin-yml.bungee") version "0.6.0"
}

stickynote {
    modules(StickyNoteModules.BUNGEECORD)
}

dependencies {
    compileOnly(libs.bungeecord.api)

    api(project(":sayanvanish-proxy"))
}

modrinth {
    loaders.set(listOf("bungeecord", "waterfall"))
    detectLoaders.set(false)
}

bungee {
    name = rootProject.name
    version = rootProject.version as String
    description = rootProject.description

    main = "${rootProject.group}.${findProperty("slug")!! as String}.bungeecord.${rootProject.name}"

    author = findProperty("author")!! as String
}

modrinth {
    loaders.set(listOf(
        "bungeecord",
        "waterfall",
    ))
}