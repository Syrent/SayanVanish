import org.sayandev.*
import org.sayandev.applyShadowRelocation

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