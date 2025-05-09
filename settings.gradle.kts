pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()

        maven("https://repo.sayandev.org/snapshots")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
    id("org.sayandev.stickynote.settings") version "1.9.1.10"
}

rootProject.name = "SayanVanish"

include("sayanvanish-api")
include("sayanvanish-bukkit")
include("sayanvanish-proxy")
include("sayanvanish-proxy:sayanvanish-proxy-velocity")
include("sayanvanish-proxy:sayanvanish-proxy-bungeecord")