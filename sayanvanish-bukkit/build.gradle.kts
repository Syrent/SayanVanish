import org.sayandev.*
import org.sayandev.applyShadowRelocation

plugins {
    id("xyz.jpenilla.run-paper") version "2.3.0"
    id("io.papermc.paperweight.userdev") version "1.7.1"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
}

generateRepositoriesClass(Module.BUKKIT)
generateTemplate(Module.BUKKIT)

repositories {
    applyRepositories(Module.BUKKIT)
}

dependencies {
    paperweight.paperDevBundle("1.20.4-R0.1-SNAPSHOT")
    implementation(project(":sayanvanish-api"))

    applyDependencies(Module.BUKKIT)
}

tasks {
    shadowJar {
        applyShadowRelocation(Module.BUKKIT)
    }

    java {
        if (gradle.startParameter.getTaskNames().isNotEmpty() && gradle.startParameter.getTaskNames().contains("runServer")) {
            toolchain.languageVersion = JavaLanguageVersion.of(21)
        }
    }

    runServer {
        minecraftVersion("1.20.6")

        downloadPlugins {
            url("https://download.luckperms.net/1539/bukkit/loader/LuckPerms-Bukkit-5.4.126.jar")
//            url("https://github.com/Insprill/custom-join-messages/releases/download/v17.5.0/custom-join-messages-17.5.0.jar")
            url("https://github.com/EssentialsX/Essentials/releases/download/2.20.1/EssentialsX-2.20.1.jar")
        }
    }

    runPaper {
        folia.registerTask()
    }
}

bukkit {
    name = rootProject.name
    version = rootProject.version as String
    description = rootProject.description
    website = findProperty("website")!! as String
    author = findProperty("author")!! as String

    main = "${rootProject.group}.${findProperty("slug")!! as String}.bukkit.${rootProject.name}"

    foliaSupported = true

    apiVersion = "1.13"

    softDepend = listOf(
        "Essentials",
        "squaremap",
        "LuckPerms",
        "PlaceholderAPI",
        "Citizens"
    )
}