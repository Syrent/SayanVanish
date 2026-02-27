pluginManagement {
    val useStickyNoteSubmodule = providers.gradleProperty("stickynote.useSubmodule").orNull?.toBoolean() == true
    val stickyNoteSubmodulePath = providers.gradleProperty("stickynote.submodulePath").orNull ?: "StickyNote"
    if (useStickyNoteSubmodule) {
        includeBuild(stickyNoteSubmodulePath)
    }

    repositories {
        mavenLocal()
        gradlePluginPortal()

        maven("https://repo.sayandev.org/snapshots")
    }
}

plugins {
    id("org.sayandev.stickynote.settings") version "2.0.0-alpha.38"
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "SayanVanish"

include("sayanvanish-api")
include("sayanvanish-paper")
include("sayanvanish-proxy")
include("sayanvanish-proxy:sayanvanish-proxy-velocity")
