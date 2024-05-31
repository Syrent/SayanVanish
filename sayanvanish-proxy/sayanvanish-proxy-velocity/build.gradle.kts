plugins {
    id("xyz.jpenilla.run-velocity") version "2.3.0"
}

repositories {
    // Velocity
    maven("https://repo.papermc.io/repository/maven-public/")

    // libby
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    compileOnly("org.sayandev:stickynote-proxy-velocity:1.0.32")

    implementation("com.alessiodp.libby:libby-velocity:2.0.0-SNAPSHOT")

    compileOnly("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")

    implementation(project(":sayanvanish-proxy"))
}

tasks {
    runVelocity {
        velocityVersion("3.3.0-SNAPSHOT")
    }
}