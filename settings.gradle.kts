pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

rootProject.name = "SayanVanish"

include("sayanvanish-api")
include("sayanvanish-bukkit")
include("sayanvanish-proxy")
include("sayanvanish-proxy:sayanvanish-proxy-velocity")
