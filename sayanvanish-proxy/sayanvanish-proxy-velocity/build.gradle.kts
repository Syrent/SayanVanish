import org.sayandev.*
import org.sayandev.applyShadowRelocation
import org.sayandev.plugin.StickyNoteModules

plugins {
    id("xyz.jpenilla.run-velocity") version "2.3.0"
}

stickynote {
    modules(StickyNoteModules.VELOCITY)
}

repositories {
    applyRepositories(Module.VELOCITY)
}

dependencies {
    applyDependencies(Module.VELOCITY)

    api(project(":sayanvanish-proxy"))
}

tasks {
    shadowJar {
        applyShadowRelocation(Module.VELOCITY)
    }

    runVelocity {
        velocityVersion("3.3.0-SNAPSHOT")

        downloadPlugins {
            url("https://download.luckperms.net/1543/velocity/LuckPerms-Velocity-5.4.130.jar")
            url("https://cdn.modrinth.com/data/Q10irTG0/versions/AWUdIWE9/Velocitab-1.6.5.jar")
            url("https://github.com/Syrent/EnhancedVelocity/releases/download/1.3.3/EnhancedVelocity.v1.3.3.jar")
            url("https://hangarcdn.papermc.io/plugins/Andre_601/AdvancedServerList/versions/4.11.1/VELOCITY/AdvancedServerList-Velocity-4.11.1.jar")
        }
    }
}