plugins {
    id("xyz.jpenilla.run-velocity") version "2.3.0"
}

repositories {
    // Velocity
    maven("https://repo.papermc.io/repository/maven-public/")

    // libby
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")

    // Velocitab
    maven("https://repo.william278.net/releases")
}

dependencies {
    compileOnly("org.sayandev:stickynote-proxy-velocity:1.0.32")

    implementation("com.alessiodp.libby:libby-velocity:2.0.0-SNAPSHOT")

    compileOnly("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")

    compileOnly("ir.syrent:EnhancedVelocity:1.3.3")
    compileOnly("net.william278:velocitab:1.6.5")

    implementation(project(":sayanvanish-proxy"))
}

tasks {
    runVelocity {
        velocityVersion("3.3.0-SNAPSHOT")

        downloadPlugins {
            url("https://download.luckperms.net/1543/velocity/LuckPerms-Velocity-5.4.130.jar")
            url("https://cdn.modrinth.com/data/Q10irTG0/versions/AWUdIWE9/Velocitab-1.6.5.jar")
//            url("https://github.com/Syrent/EnhancedVelocity/releases/download/1.3.3/EnhancedVelocity.v1.3.3.jar")
        }
    }
}