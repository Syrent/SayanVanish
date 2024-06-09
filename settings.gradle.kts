pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()

        maven("https://repo.sayandev.org/snapshots")
    }
}

rootProject.name = "SayanVanish"

include("sayanvanish-api")
include("sayanvanish-bukkit")
include("sayanvanish-proxy")
include("sayanvanish-proxy:sayanvanish-proxy-velocity")
include("sayanvanish-proxy:sayanvanish-proxy-bungeecord")