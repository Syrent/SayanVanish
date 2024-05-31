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
    compileOnly("org.sayandev:stickynote-core:1.0.31")
    compileOnly("org.sayandev:stickynote-proxy-velocity:1.0.31")
    compileOnly("com.mysql:mysql-connector-j:8.4.0")
    compileOnly("org.xerial:sqlite-jdbc:3.46.0.0")

    implementation("com.alessiodp.libby:libby-velocity:2.0.0-SNAPSHOT")

    compileOnly("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")
    implementation(kotlin("stdlib", version = "2.0.0"))
    // TODO: Try to use libby for kotlin reflect
//    api(kotlin("reflect"))

//    compileOnly(project(":sayanvanish-proxy-velocity"))
    implementation(project(":sayanvanish-api"))
}

tasks {
    runVelocity {
        velocityVersion("3.3.0-SNAPSHOT")
    }
}