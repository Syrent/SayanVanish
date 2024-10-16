import org.sayandev.plugin.StickyNoteModules

plugins {
    id("xyz.jpenilla.run-velocity") version "2.3.0"
}

stickynote {
    modules(StickyNoteModules.VELOCITY)
}

dependencies {
    compileOnly(libs.velocity.api)
    compileOnly(libs.velocitab)
    compileOnly(libs.enhancedvelocity)
    annotationProcessor(libs.velocity.api)

    api(project(":sayanvanish-proxy"))
}

tasks {
    runVelocity {
        velocityVersion("3.3.0-SNAPSHOT")

        downloadPlugins {
            url("https://download.luckperms.net/1556/velocity/LuckPerms-Velocity-5.4.141.jar")
            url("https://cdn.modrinth.com/data/Q10irTG0/versions/JPcYpUcS/Velocitab-1.7.1-cf7c92e.jar")
            url("https://github.com/Syrent/EnhancedVelocity/releases/download/1.3.3/EnhancedVelocity.v1.3.3.jar")
            url("https://cdn.modrinth.com/data/HQyibRsN/versions/FfO1vuOg/MiniPlaceholders-Velocity-2.2.4.jar")
//            url("https://hangarcdn.papermc.io/plugins/Andre_601/AdvancedServerList/versions/4.11.1/VELOCITY/AdvancedServerList-Velocity-4.11.1.jar")
//            url("https://ci.lucko.me/job/spark/419/artifact/spark-velocity/build/libs/spark-1.10.74-velocity.jar")
        }
    }
}