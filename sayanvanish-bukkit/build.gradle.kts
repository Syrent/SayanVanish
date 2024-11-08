import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import org.sayandev.*
import org.sayandev.plugin.StickyNoteModules

plugins {
    id("xyz.jpenilla.run-paper") version "2.3.0"
//    id("io.papermc.paperweight.userdev") version "1.7.2"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
}

stickynote {
    modules(StickyNoteModules.BUKKIT, StickyNoteModules.BUKKIT_NMS)
}

dependencies {
    compileOnly(libs.paper.api)
    compileOnly(libs.folia.api)
    compileOnly(libs.placeholderapi)
    compileOnly(libs.essentialsx)
    compileOnly(libs.citizens.api)
    compileOnly(libs.squaremap.api)

//    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
//    paperweight.paperDevBundle("1.21.1-R0.1-SNAPSHOT")
    implementation(project(":sayanvanish-api"))
}

tasks {
    jar {
        manifest {
            attributes["paperweight-mappings-namespace"] = "mojang"
        }
    }

    shadowJar {
        manifest {
            attributes["paperweight-mappings-namespace"] = "mojang"
        }
    }

    java {
        if (gradle.startParameter.getTaskNames().isNotEmpty() && (gradle.startParameter.getTaskNames().contains("runServer") || gradle.startParameter.getTaskNames().contains("runFolia"))) {
            toolchain.languageVersion = JavaLanguageVersion.of(21)
        }
    }

    val deleteSayanDevLib = register<Delete>("deleteSayanDevLib") {
        delete(file("$projectDir/${project.name.lowercase()}-bukkit/run/plugins/${project.name.lowercase()}/lib/org/sayandev"))
    }

    runServer {
//        dependsOn(deleteSayanDevLib)
        minecraftVersion("1.21.1")

        downloadPlugins {
            hangar("ViaVersion", "5.0.4-SNAPSHOT+548")
//            url("https://github.com/NEZNAMY/TAB/releases/download/4.1.6/TAB.v4.1.6.jar")
//            url("https://ci.lucko.me/job/spark/418/artifact/spark-bukkit/build/libs/spark-1.10.73-bukkit.jar")
//            url("https://github.com/Insprill/custom-join-messages/releases/download/v17.5.0/custom-join-messages-17.5.0.jar")
            url("https://github.com/EssentialsX/Essentials/releases/download/2.20.1/EssentialsX-2.20.1.jar")
            url("https://download.luckperms.net/1552/bukkit/loader/LuckPerms-Bukkit-5.4.137.jar")
        }
    }

    runPaper {
        folia.registerTask()
    }

//    paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION
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

    permissions {
        register("sayanvanish.action.vanish.onjoin") {
            default = BukkitPluginDescription.Permission.Default.FALSE
        }
    }

    softDepend = listOf(
        "Essentials",
        "squaremap",
        "LuckPerms",
        "PlaceholderAPI",
        "MiniPlaceholders",
        "Citizens",
        "packetevents"
    )
}

// Define a resolvable configuration extending from 'api'
configurations {
    create("apiResolved") {
        isCanBeResolved = true
        extendsFrom(configurations.getByName("compileOnlyApi"))
    }
}

// Task to print all 'api' dependencies, including transitives
tasks.register("printApiDependencies") {
    doLast {
        configurations.getByName("apiResolved").resolvedConfiguration.resolvedArtifacts.forEach { artifact ->
            println("API Dependency: ${artifact.moduleVersion.id.group}:${artifact.name}:${artifact.moduleVersion.id.version}")
        }
    }
}
